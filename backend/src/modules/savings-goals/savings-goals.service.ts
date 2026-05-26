import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { CreateSavingsGoalDto } from './dto/create-savings-goal.dto';
import { UpdateSavingsGoalDto } from './dto/update-savings-goal.dto';
import { Prisma } from '@prisma/client';

@Injectable()
export class SavingsGoalsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string) {
    const goals = await this.prisma.savingsGoal.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
    });

    return goals.map(g => ({
      ...g,
      targetAmount: Number(g.targetAmount),
      currentAmount: Number(g.currentAmount),
      progressPercentage: Number(g.targetAmount) > 0 ? (Number(g.currentAmount) / Number(g.targetAmount)) * 100 : 0,
    }));
  }

  async findOne(userId: string, id: string) {
    const goal = await this.prisma.savingsGoal.findFirst({
      where: { id, userId },
    });

    if (!goal) {
      throw new NotFoundException('Savings goal not found');
    }

    return {
      ...goal,
      targetAmount: Number(goal.targetAmount),
      currentAmount: Number(goal.currentAmount),
      progressPercentage: Number(goal.targetAmount) > 0 ? (Number(goal.currentAmount) / Number(goal.targetAmount)) * 100 : 0,
    };
  }

  async create(userId: string, dto: CreateSavingsGoalDto) {
    const targetAmountDecimal = new Prisma.Decimal(dto.targetAmount);
    const currentAmountDecimal = dto.currentAmount !== undefined ? new Prisma.Decimal(dto.currentAmount) : new Prisma.Decimal(0);
    const targetDate = dto.targetDate ? new Date(dto.targetDate) : null;
    const isCompleted = Number(currentAmountDecimal) >= Number(targetAmountDecimal);

    const goal = await this.prisma.savingsGoal.create({
      data: {
        userId,
        name: dto.name,
        targetAmount: targetAmountDecimal,
        currentAmount: currentAmountDecimal,
        targetDate,
        icon: dto.icon || 'flag',
        color: dto.color || '#3B82F6',
        isCompleted,
      },
    });

    return {
      ...goal,
      targetAmount: Number(goal.targetAmount),
      currentAmount: Number(goal.currentAmount),
    };
  }

  async update(userId: string, id: string, dto: UpdateSavingsGoalDto) {
    const existing = await this.prisma.savingsGoal.findFirst({
      where: { id, userId },
    });

    if (!existing) {
      throw new NotFoundException('Savings goal not found');
    }

    const targetAmount = dto.targetAmount !== undefined ? new Prisma.Decimal(dto.targetAmount) : existing.targetAmount;
    const currentAmount = dto.currentAmount !== undefined ? new Prisma.Decimal(dto.currentAmount) : existing.currentAmount;
    const isCompleted = Number(currentAmount) >= Number(targetAmount);

    const goal = await this.prisma.savingsGoal.update({
      where: { id },
      data: {
        name: dto.name || undefined,
        targetAmount,
        currentAmount,
        targetDate: dto.targetDate ? new Date(dto.targetDate) : undefined,
        icon: dto.icon || undefined,
        color: dto.color || undefined,
        isCompleted,
      },
    });

    return {
      ...goal,
      targetAmount: Number(goal.targetAmount),
      currentAmount: Number(goal.currentAmount),
    };
  }

  async remove(userId: string, id: string) {
    const existing = await this.prisma.savingsGoal.findFirst({
      where: { id, userId },
    });

    if (!existing) {
      throw new NotFoundException('Savings goal not found');
    }

    return this.prisma.savingsGoal.delete({
      where: { id },
    });
  }
}
