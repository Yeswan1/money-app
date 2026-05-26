import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { CreateSubscriptionDto } from './dto/create-subscription.dto';
import { UpdateSubscriptionDto } from './dto/update-subscription.dto';
import { Prisma } from '@prisma/client';

@Injectable()
export class SubscriptionsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string) {
    const subscriptions = await this.prisma.subscription.findMany({
      where: { userId },
      orderBy: { nextBillingDate: 'asc' },
    });

    return subscriptions.map(s => ({
      ...s,
      amount: Number(s.amount),
    }));
  }

  async findOne(userId: string, id: string) {
    const sub = await this.prisma.subscription.findFirst({
      where: { id, userId },
    });

    if (!sub) {
      throw new NotFoundException('Subscription not found');
    }

    return {
      ...sub,
      amount: Number(sub.amount),
    };
  }

  async create(userId: string, dto: CreateSubscriptionDto) {
    const amountDecimal = new Prisma.Decimal(dto.amount);
    const nextBillingDate = new Date(dto.nextBillingDate);

    const sub = await this.prisma.subscription.create({
      data: {
        userId,
        name: dto.name,
        amount: amountDecimal,
        billingCycle: dto.billingCycle,
        nextBillingDate,
        color: dto.color || '#3B82F6',
        isActive: dto.isActive !== undefined ? dto.isActive : true,
      },
    });

    return {
      ...sub,
      amount: Number(sub.amount),
    };
  }

  async update(userId: string, id: string, dto: UpdateSubscriptionDto) {
    const existing = await this.prisma.subscription.findFirst({
      where: { id, userId },
    });

    if (!existing) {
      throw new NotFoundException('Subscription not found');
    }

    const amount = dto.amount !== undefined ? new Prisma.Decimal(dto.amount) : existing.amount;
    const nextBillingDate = dto.nextBillingDate ? new Date(dto.nextBillingDate) : existing.nextBillingDate;

    const sub = await this.prisma.subscription.update({
      where: { id },
      data: {
        name: dto.name || undefined,
        amount,
        billingCycle: dto.billingCycle || undefined,
        nextBillingDate,
        color: dto.color || undefined,
        isActive: dto.isActive !== undefined ? dto.isActive : undefined,
      },
    });

    return {
      ...sub,
      amount: Number(sub.amount),
    };
  }

  async remove(userId: string, id: string) {
    const existing = await this.prisma.subscription.findFirst({
      where: { id, userId },
    });

    if (!existing) {
      throw new NotFoundException('Subscription not found');
    }

    return this.prisma.subscription.delete({
      where: { id },
    });
  }
}
