import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { CreateTransactionDto } from './dto/create-transaction.dto';
import { UpdateTransactionDto } from './dto/update-transaction.dto';
import { Prisma } from '@prisma/client';

@Injectable()
export class TransactionsService {
  constructor(private prisma: PrismaService) {}

  async findAll(
    userId: string,
    filters: {
      startDate?: string;
      endDate?: string;
      type?: 'INCOME' | 'EXPENSE';
      categoryId?: string;
      search?: string;
      tag?: string;
      limit?: number;
      offset?: number;
    },
  ) {
    const where: Prisma.TransactionWhereInput = {
      userId,
      deletedAt: null,
    };

    if (filters.startDate || filters.endDate) {
      where.transactionDate = {};
      if (filters.startDate) {
        where.transactionDate.gte = new Date(filters.startDate);
      }
      if (filters.endDate) {
        where.transactionDate.lte = new Date(filters.endDate);
      }
    }

    if (filters.type) {
      where.type = filters.type;
    }

    if (filters.categoryId) {
      where.categoryId = filters.categoryId;
    }

    if (filters.search) {
      where.OR = [
        {
          description: {
            contains: filters.search,
            mode: 'insensitive',
          },
        },
        {
          category: {
            name: {
              contains: filters.search,
              mode: 'insensitive',
            },
          },
        },
        {
          tags: {
            some: {
              tag: {
                contains: filters.search,
                mode: 'insensitive',
              },
            },
          },
        },
      ];
    }

    if (filters.tag) {
      where.tags = {
        some: {
          tag: {
            equals: filters.tag,
            mode: 'insensitive',
          },
        },
      };
    }

    const totalCount = await this.prisma.transaction.count({ where });

    const transactions = await this.prisma.transaction.findMany({
      where,
      include: {
        category: true,
        tags: {
          select: {
            tag: true,
          },
        },
      },
      orderBy: {
        transactionDate: 'desc',
      },
      take: filters.limit ? Number(filters.limit) : undefined,
      skip: filters.offset ? Number(filters.offset) : undefined,
    });

    return {
      total: totalCount,
      transactions: transactions.map(t => ({
        ...t,
        tags: t.tags.map(tagObj => tagObj.tag),
      })),
    };
  }

  async findOne(userId: string, id: string) {
    const transaction = await this.prisma.transaction.findFirst({
      where: { id, userId, deletedAt: null },
      include: {
        category: true,
        tags: {
          select: {
            tag: true,
          },
        },
      },
    });

    if (!transaction) {
      throw new NotFoundException('Transaction not found or deleted');
    }

    return {
      ...transaction,
      tags: transaction.tags.map(t => t.tag),
    };
  }

  async create(userId: string, dto: CreateTransactionDto) {
    // Validate category
    const category = await this.prisma.category.findFirst({
      where: {
        id: dto.categoryId,
        OR: [
          { isSystem: true },
          { userId },
        ],
      },
    });

    if (!category) {
      throw new NotFoundException('Selected category was not found');
    }

    const transactionDate = new Date(dto.transactionDate);
    const amountDecimal = new Prisma.Decimal(dto.amount);

    return await this.prisma.$transaction(async (tx) => {
      // 1. Create transaction
      const transaction = await tx.transaction.create({
        data: {
          userId,
          categoryId: dto.categoryId,
          amount: amountDecimal,
          type: dto.type,
          description: dto.description,
          transactionDate,
        },
      });

      // 2. Handle Tags
      if (dto.tags && dto.tags.length > 0) {
        const uniqueTags = Array.from(new Set(dto.tags.map(t => t.trim().toLowerCase())));
        await tx.transactionTag.createMany({
          data: uniqueTags.map(tag => ({
            transactionId: transaction.id,
            tag,
          })),
        });
      }

      // 3. Check budget warnings (only for EXPENSE types)
      let budgetWarning = false;
      let currentSpending = 0;
      let budgetLimit = 0;

      if (dto.type === 'EXPENSE') {
        const month = transactionDate.getMonth() + 1; // 1-12
        const year = transactionDate.getFullYear();

        const budget = await tx.budget.findUnique({
          where: {
            userId_categoryId_month_year: {
              userId,
              categoryId: dto.categoryId,
              month,
              year,
            },
          },
        });

        if (budget) {
          budgetLimit = Number(budget.amount);
          
          // Calculate historical spending in this category for this month
          const startOfMonth = new Date(year, month - 1, 1);
          const endOfMonth = new Date(year, month, 0); // last day of month

          const expensesSum = await tx.transaction.aggregate({
            _sum: {
              amount: true,
            },
            where: {
              userId,
              categoryId: dto.categoryId,
              type: 'EXPENSE',
              deletedAt: null,
              transactionDate: {
                gte: startOfMonth,
                lte: endOfMonth,
              },
            },
          });

          const spentSoFar = expensesSum._sum.amount ? Number(expensesSum._sum.amount) : 0;
          currentSpending = spentSoFar;

          if (currentSpending > budgetLimit) {
            budgetWarning = true;
          }
        }
      }

      // Query complete transaction to return
      const fullTransaction = await tx.transaction.findUnique({
        where: { id: transaction.id },
        include: {
          category: true,
          tags: {
            select: { tag: true },
          },
        },
      });

      return {
        transaction: {
          ...fullTransaction,
          tags: fullTransaction?.tags.map(t => t.tag) || [],
        },
        budgetWarning,
        budgetLimit,
        currentSpending,
      };
    });
  }

  async update(userId: string, id: string, dto: UpdateTransactionDto) {
    const existing = await this.prisma.transaction.findFirst({
      where: { id, userId, deletedAt: null },
    });

    if (!existing) {
      throw new NotFoundException('Transaction not found');
    }

    if (dto.categoryId) {
      const category = await this.prisma.category.findFirst({
        where: {
          id: dto.categoryId,
          OR: [
            { isSystem: true },
            { userId },
          ],
        },
      });
      if (!category) {
        throw new NotFoundException('Selected category was not found');
      }
    }

    return await this.prisma.$transaction(async (tx) => {
      // Update basic fields
      const updated = await tx.transaction.update({
        where: { id },
        data: {
          categoryId: dto.categoryId || undefined,
          amount: dto.amount !== undefined ? new Prisma.Decimal(dto.amount) : undefined,
          type: dto.type || undefined,
          description: dto.description !== undefined ? dto.description : undefined,
          transactionDate: dto.transactionDate ? new Date(dto.transactionDate) : undefined,
        },
      });

      // Update tags if provided
      if (dto.tags !== undefined) {
        // Delete existing tags
        await tx.transactionTag.deleteMany({
          where: { transactionId: id },
        });

        if (dto.tags.length > 0) {
          const uniqueTags = Array.from(new Set(dto.tags.map(t => t.trim().toLowerCase())));
          await tx.transactionTag.createMany({
            data: uniqueTags.map(tag => ({
              transactionId: id,
              tag,
            })),
          });
        }
      }

      // Re-query full transaction
      const fullTransaction = await tx.transaction.findUnique({
        where: { id },
        include: {
          category: true,
          tags: {
            select: { tag: true },
          },
        },
      });

      return {
        ...fullTransaction,
        tags: fullTransaction?.tags.map(t => t.tag) || [],
      };
    });
  }

  async remove(userId: string, id: string) {
    const existing = await this.prisma.transaction.findFirst({
      where: { id, userId, deletedAt: null },
    });

    if (!existing) {
      throw new NotFoundException('Transaction not found');
    }

    // Soft delete
    return this.prisma.transaction.update({
      where: { id },
      data: {
        deletedAt: new Date(),
      },
    });
  }
}
