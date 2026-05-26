import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { SetBudgetDto } from './dto/set-budget.dto';
import { Prisma } from '@prisma/client';

@Injectable()
export class BudgetsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string, month: number, year: number) {
    // Find all budgets for this month/year
    const budgets = await this.prisma.budget.findMany({
      where: {
        userId,
        month,
        year,
      },
      include: {
        category: true,
      },
    });

    // For each budget, calculate spent amount in this month
    const startOfMonth = new Date(year, month - 1, 1);
    const endOfMonth = new Date(year, month, 0);

    const budgetsWithSpend = await Promise.all(
      budgets.map(async (b) => {
        const spentAgg = await this.prisma.transaction.aggregate({
          _sum: { amount: true },
          where: {
            userId,
            categoryId: b.categoryId,
            type: 'EXPENSE',
            deletedAt: null,
            transactionDate: {
              gte: startOfMonth,
              lte: endOfMonth,
            },
          },
        });

        const spent = spentAgg._sum.amount ? Number(spentAgg._sum.amount) : 0;
        return {
          id: b.id,
          categoryId: b.categoryId,
          categoryName: b.category.name,
          color: b.category.color,
          icon: b.category.icon,
          limit: Number(b.amount),
          spent,
          remaining: Number(b.amount) - spent,
          utilizationPercentage: Number(b.amount) > 0 ? (spent / Number(b.amount)) * 100 : 0,
        };
      }),
    );

    return budgetsWithSpend;
  }

  async setBudget(userId: string, dto: SetBudgetDto) {
    // Ensure category exists
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

    const amountDecimal = new Prisma.Decimal(dto.amount);

    return this.prisma.budget.upsert({
      where: {
        userId_categoryId_month_year: {
          userId,
          categoryId: dto.categoryId,
          month: dto.month,
          year: dto.year,
        },
      },
      update: {
        amount: amountDecimal,
      },
      create: {
        userId,
        categoryId: dto.categoryId,
        amount: amountDecimal,
        month: dto.month,
        year: dto.year,
      },
      include: {
        category: true,
      },
    });
  }

  async getSummary(userId: string, month: number, year: number) {
    const startOfMonth = new Date(year, month - 1, 1);
    const endOfMonth = new Date(year, month, 0);

    // Get total budgeted
    const budgets = await this.prisma.budget.findMany({
      where: { userId, month, year },
    });
    const totalBudgeted = budgets.reduce((sum, b) => sum + Number(b.amount), 0);

    // Get total expense spent in this month
    const totalSpentAgg = await this.prisma.transaction.aggregate({
      _sum: { amount: true },
      where: {
        userId,
        type: 'EXPENSE',
        deletedAt: null,
        transactionDate: {
          gte: startOfMonth,
          lte: endOfMonth,
        },
      },
    });
    const totalSpent = totalSpentAgg._sum.amount ? Number(totalSpentAgg._sum.amount) : 0;

    // Get breakdown by category
    const categoriesBudget = await this.findAll(userId, month, year);

    return {
      totalBudgeted,
      totalSpent,
      remainingBudget: totalBudgeted - totalSpent,
      overallUtilization: totalBudgeted > 0 ? (totalSpent / totalBudgeted) * 100 : 0,
      breakdown: categoriesBudget,
    };
  }
}
