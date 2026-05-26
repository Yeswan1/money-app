import { Injectable } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';

@Injectable()
export class ReportsService {
  constructor(private prisma: PrismaService) {}

  async getDashboardStats(userId: string) {
    const today = new Date();
    const month = today.getMonth() + 1;
    const year = today.getFullYear();
    const startOfMonth = new Date(year, month - 1, 1);
    const endOfMonth = new Date(year, month, 0);

    // 1. Current month expenses and income
    const transactionAgg = await this.prisma.transaction.groupBy({
      by: ['type'],
      where: {
        userId,
        deletedAt: null,
        transactionDate: {
          gte: startOfMonth,
          lte: endOfMonth,
        },
      },
      _sum: { amount: true },
    });

    let monthlyExpenses = 0;
    let monthlyIncome = 0;

    for (const group of transactionAgg) {
      if (group.type === 'EXPENSE' && group._sum.amount) {
        monthlyExpenses = Number(group._sum.amount);
      } else if (group.type === 'INCOME' && group._sum.amount) {
        monthlyIncome = Number(group._sum.amount);
      }
    }

    // 2. Active budgets overview
    const budgets = await this.prisma.budget.findMany({
      where: { userId, month, year },
      include: { category: true },
    });

    const activeBudgets = await Promise.all(
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
          categoryName: b.category.name,
          color: b.category.color,
          limit: Number(b.amount),
          spent,
          utilizationPercentage: Number(b.amount) > 0 ? (spent / Number(b.amount)) * 100 : 0,
        };
      }),
    );

    // 3. Savings goals overview
    const goals = await this.prisma.savingsGoal.findMany({
      where: { userId },
    });
    const totalSavingsTarget = goals.reduce((sum, g) => sum + Number(g.targetAmount), 0);
    const totalSavingsCurrent = goals.reduce((sum, g) => sum + Number(g.currentAmount), 0);

    // 4. Recent transactions (last 5)
    const recentTransactions = await this.prisma.transaction.findMany({
      where: { userId, deletedAt: null },
      include: { category: true },
      orderBy: { transactionDate: 'desc' },
      take: 5,
    });

    return {
      monthlySpent: monthlyExpenses,
      monthlyIncome,
      netSavings: monthlyIncome - monthlyExpenses,
      savingsOverview: {
        totalTarget: totalSavingsTarget,
        totalCurrent: totalSavingsCurrent,
        progressPercentage: totalSavingsTarget > 0 ? (totalSavingsCurrent / totalSavingsTarget) * 100 : 0,
      },
      budgets: activeBudgets,
      recentTransactions: recentTransactions.map(t => ({
        id: t.id,
        amount: Number(t.amount),
        type: t.type,
        description: t.description,
        transactionDate: t.transactionDate,
        category: t.category.name,
        color: t.category.color,
        icon: t.category.icon,
      })),
    };
  }

  async getWeeklyReport(userId: string) {
    const today = new Date();
    const oneWeekAgo = new Date();
    oneWeekAgo.setDate(today.getDate() - 7);

    // Get expenses of last 7 days
    const expenses = await this.prisma.transaction.findMany({
      where: {
        userId,
        type: 'EXPENSE',
        deletedAt: null,
        transactionDate: {
          gte: oneWeekAgo,
          lte: today,
        },
      },
      include: { category: true },
      orderBy: { transactionDate: 'asc' },
    });

    // Group by day name
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const dailyExpenses: Record<string, number> = {};
    
    // Initialize last 7 days
    for (let i = 0; i < 7; i++) {
      const d = new Date();
      d.setDate(today.getDate() - i);
      const dayName = days[d.getDay()];
      dailyExpenses[dayName] = 0;
    }

    const categoryBreakdown: Record<string, { amount: number; color: string; icon: string }> = {};

    for (const e of expenses) {
      const dayName = days[new Date(e.transactionDate).getDay()];
      if (dailyExpenses[dayName] !== undefined) {
        dailyExpenses[dayName] += Number(e.amount);
      }

      const catName = e.category.name;
      if (!categoryBreakdown[catName]) {
        categoryBreakdown[catName] = { amount: 0, color: e.category.color, icon: e.category.icon };
      }
      categoryBreakdown[catName].amount += Number(e.amount);
    }

    const totalSpent = expenses.reduce((sum, e) => sum + Number(e.amount), 0);

    return {
      totalSpent,
      averageDailySpent: totalSpent / 7,
      dailyTrend: Object.entries(dailyExpenses).map(([day, amount]) => ({ day, amount })),
      breakdown: Object.entries(categoryBreakdown).map(([category, details]) => ({
        category,
        amount: details.amount,
        percentage: totalSpent > 0 ? (details.amount / totalSpent) * 100 : 0,
        color: details.color,
        icon: details.icon,
      })),
    };
  }

  async getMonthlyReport(userId: string) {
    const today = new Date();
    const month = today.getMonth() + 1;
    const year = today.getFullYear();
    const startOfMonth = new Date(year, month - 1, 1);
    const endOfMonth = new Date(year, month, 0);

    // Fetch this month's expenses
    const currentExpenses = await this.prisma.transaction.findMany({
      where: {
        userId,
        type: 'EXPENSE',
        deletedAt: null,
        transactionDate: {
          gte: startOfMonth,
          lte: endOfMonth,
        },
      },
      include: { category: true },
    });

    const totalSpent = currentExpenses.reduce((sum, e) => sum + Number(e.amount), 0);

    // Fetch previous month's expenses
    const prevMonth = month === 1 ? 12 : month - 1;
    const prevYear = month === 1 ? year - 1 : year;
    const startOfPrevMonth = new Date(prevYear, prevMonth - 1, 1);
    const endOfPrevMonth = new Date(prevYear, prevMonth, 0);

    const prevMonthSpentAgg = await this.prisma.transaction.aggregate({
      _sum: { amount: true },
      where: {
        userId,
        type: 'EXPENSE',
        deletedAt: null,
        transactionDate: {
          gte: startOfPrevMonth,
          lte: endOfPrevMonth,
        },
      },
    });
    const prevMonthSpent = prevMonthSpentAgg._sum.amount ? Number(prevMonthSpentAgg._sum.amount) : 0;

    // Category breakdown
    const categoryBreakdown: Record<string, { amount: number; color: string; icon: string }> = {};
    for (const e of currentExpenses) {
      const catName = e.category.name;
      if (!categoryBreakdown[catName]) {
        categoryBreakdown[catName] = { amount: 0, color: e.category.color, icon: e.category.icon };
      }
      categoryBreakdown[catName].amount += Number(e.amount);
    }

    return {
      totalSpent,
      previousMonthSpent: prevMonthSpent,
      percentageChange: prevMonthSpent > 0 ? ((totalSpent - prevMonthSpent) / prevMonthSpent) * 100 : 0,
      breakdown: Object.entries(categoryBreakdown).map(([category, details]) => ({
        category,
        amount: details.amount,
        percentage: totalSpent > 0 ? (details.amount / totalSpent) * 100 : 0,
        color: details.color,
        icon: details.icon,
      })),
    };
  }

  async getSpendingTrends(userId: string) {
    const today = new Date();
    const trends = [];

    // Compile logs for the last 6 months
    for (let i = 5; i >= 0; i--) {
      const date = new Date(today.getFullYear(), today.getMonth() - i, 1);
      const m = date.getMonth() + 1;
      const y = date.getFullYear();

      const startOfMonth = new Date(y, m - 1, 1);
      const endOfMonth = new Date(y, m, 0);

      const monthlyAgg = await this.prisma.transaction.groupBy({
        by: ['type'],
        where: {
          userId,
          deletedAt: null,
          transactionDate: {
            gte: startOfMonth,
            lte: endOfMonth,
          },
        },
        _sum: { amount: true },
      });

      let expenses = 0;
      let income = 0;

      for (const group of monthlyAgg) {
        if (group.type === 'EXPENSE' && group._sum.amount) {
          expenses = Number(group._sum.amount);
        } else if (group.type === 'INCOME' && group._sum.amount) {
          income = Number(group._sum.amount);
        }
      }

      trends.push({
        monthName: date.toLocaleString('default', { month: 'short' }),
        year: y,
        expenses,
        income,
        savings: income - expenses,
      });
    }

    return trends;
  }
}
