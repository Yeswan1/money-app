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

    // 5. Dynamic Alerts and Tips
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: { currency: true, role: true, profile: true },
    });
    const currency = user?.currency || 'INR';
    const symbol = currency === 'USD' ? '$' : '₹';

    const alerts: any[] = [];
    const tips: any[] = [];

    // 5a. Budget overrun and near-limit alerts
    for (const ab of activeBudgets) {
      if (ab.spent > ab.limit) {
        alerts.push({
          title: `${ab.categoryName} Exceeded`,
          message: `You have exceeded your ${symbol}${ab.limit} ${ab.categoryName} budget by ${symbol}${Math.round(ab.spent - ab.limit)}.`,
          isCritical: true,
          time: 'Just now',
        });
      } else if (ab.spent >= ab.limit * 0.8 && ab.limit > 0) {
        alerts.push({
          title: `${ab.categoryName} Near Limit`,
          message: `You've used ${Math.round(ab.utilizationPercentage)}% of your ${ab.categoryName} budget.`,
          isCritical: false,
          time: 'Today',
        });
      }
    }

    // 5b. Unusual spending checks
    const prevMonth = month === 1 ? 12 : month - 1;
    const prevYear = month === 1 ? year - 1 : year;
    const startOfPrevMonth = new Date(prevYear, prevMonth - 1, 1);
    const endOfPrevMonth = new Date(prevYear, prevMonth, 0);

    const prevMonthCatAgg = await this.prisma.transaction.groupBy({
      by: ['categoryId'],
      where: {
        userId,
        type: 'EXPENSE',
        deletedAt: null,
        transactionDate: {
          gte: startOfPrevMonth,
          lte: endOfPrevMonth,
        },
      },
      _sum: { amount: true },
    });

    const prevMonthSpending: Record<string, number> = {};
    for (const group of prevMonthCatAgg) {
      if (group.categoryId && group._sum.amount) {
        prevMonthSpending[group.categoryId] = Number(group._sum.amount);
      }
    }

    const thisMonthCatAgg = await this.prisma.transaction.groupBy({
      by: ['categoryId'],
      where: {
        userId,
        type: 'EXPENSE',
        deletedAt: null,
        transactionDate: {
          gte: startOfMonth,
          lte: endOfMonth,
        },
      },
      _sum: { amount: true },
    });

    for (const group of thisMonthCatAgg) {
      const catId = group.categoryId;
      if (!catId || !group._sum.amount) continue;
      const spentThisMonth = Number(group._sum.amount);
      const spentLastMonth = prevMonthSpending[catId] || 0;

      if (spentLastMonth > 0 && spentThisMonth > spentLastMonth * 1.3 && spentThisMonth > 50) {
        const category = await this.prisma.category.findUnique({ where: { id: catId } });
        if (category) {
          const percentIncrease = ((spentThisMonth - spentLastMonth) / spentLastMonth) * 100;
          alerts.push({
            title: `Unusual Spending`,
            message: `Your ${category.name} spending was ${Math.round(percentIncrease)}% higher than last month.`,
            isCritical: true,
            time: 'This month',
          });
        }
      }
    }

    // 5bb. Role-specific alerts
    if (user) {
      if (user.role === 'STUDENT' && user.profile) {
        const allowance = Number(user.profile.monthlyAllowance || 0);
        if (allowance > 0) {
          const daysLeft = endOfMonth.getDate() - today.getDate() + 1;
          const remainingAllowance = allowance - monthlyExpenses;
          const safeSpend = daysLeft > 0 ? Math.max(0, Math.round(remainingAllowance / daysLeft)) : 0;
          alerts.push({
            title: 'Pocket Money Safe-to-Spend',
            message: `Your monthly allowance is halfway through, but you have ${daysLeft} days left. Safe-to-spend limit is ${symbol}${safeSpend}/day.`,
            isCritical: remainingAllowance < (allowance * 0.2),
            time: 'Today',
          });
        }
      } else if (user.role === 'PROFESSIONAL' && user.profile) {
        const income = Number(user.profile.monthlyIncome || 0);
        if (income > 0 && monthlyIncome > 0) {
          const allocationAmount = Math.round(monthlyIncome * 0.15);
          alerts.push({
            title: 'Payday Optimization',
            message: `Your monthly salary was credited! Would you like to allocate ${symbol}${allocationAmount} to your active savings goals?`,
            isCritical: false,
            time: 'Today',
          });
        }
      } else if (user.role === 'HOMEMAKER' && user.profile) {
        const homemakerBudget = Number(user.profile.monthlyBudget || 0);
        if (homemakerBudget > 0 && monthlyExpenses > 0) {
          const dayOfMonth = today.getDate();
          const projectedExpenses = (monthlyExpenses / dayOfMonth) * endOfMonth.getDate();
          if (projectedExpenses > homemakerBudget) {
            const projectedOverrun = Math.round(projectedExpenses - homemakerBudget);
            const percentOverrun = Math.round((projectedOverrun / homemakerBudget) * 100);
            alerts.push({
              title: 'Weekly Household Projection',
              message: `Based on grocery purchases from Week 1 and 2, your household spending will likely exceed the monthly budget by ${percentOverrun}% if velocity continues.`,
              isCritical: true,
              time: 'Today',
            });
          }
        }
      }
    }

    if (alerts.length === 0) {
      alerts.push({
        title: 'All Budgets Healthy',
        message: 'Great job! None of your budgets are exceeded or near limit.',
        isCritical: false,
        time: 'Just now',
      });
    }

    // 5c. Smart saving tips
    let foodExpenses = 0;
    const foodCats = await this.prisma.category.findMany({
      where: {
        name: {
          mode: 'insensitive',
          in: ['food', 'dining', 'dining out', 'cafe', 'restaurant', 'groceries'],
        },
      },
    });
    if (foodCats.length > 0) {
      const foodSpentAgg = await this.prisma.transaction.aggregate({
        _sum: { amount: true },
        where: {
          userId,
          categoryId: { in: foodCats.map(c => c.id) },
          type: 'EXPENSE',
          deletedAt: null,
          transactionDate: {
            gte: startOfMonth,
            lte: endOfMonth,
          },
        },
      });
      foodExpenses = foodSpentAgg._sum.amount ? Number(foodSpentAgg._sum.amount) : 0;
    }

    if (foodExpenses > 150) {
      tips.push({
        title: 'Reduce Dining Out',
        message: `You spent ${symbol}${Math.round(foodExpenses)} on Food/Dining this month. Cooking at home more often could save you some money.`,
        color: '#3B82F6',
        impact: 'High Impact',
      });
    }

    const activeSubs = await this.prisma.subscription.findMany({
      where: { userId, isActive: true },
    });
    if (activeSubs.length > 0) {
      const totalSubsSpent = activeSubs.reduce((sum, s) => sum + Number(s.amount), 0);
      tips.push({
        title: 'Cancel Unused Subscriptions',
        message: `You have ${activeSubs.length} active subscription(s) costing you ${symbol}${Math.round(totalSubsSpent)}/mo. Cancel any you haven't used recently.`,
        color: '#10B981',
        impact: 'Medium Impact',
      });
    }

    const activeGoalsCount = await this.prisma.savingsGoal.count({
      where: { userId, isCompleted: false },
    });
    if (activeGoalsCount === 0) {
      tips.push({
        title: 'Set up a Savings Goal',
        message: `You don't have any active savings goals. Creating a goal can help you stay motivated to save!`,
        color: '#F59E0B',
        impact: 'Medium Impact',
      });
    }

    let utilityExpenses = 0;
    const utilityCats = await this.prisma.category.findMany({
      where: {
        name: {
          mode: 'insensitive',
          in: ['utilities', 'utility', 'bills', 'electricity', 'water', 'gas'],
        },
      },
    });
    if (utilityCats.length > 0) {
      const utilitySpentAgg = await this.prisma.transaction.aggregate({
        _sum: { amount: true },
        where: {
          userId,
          categoryId: { in: utilityCats.map(c => c.id) },
          type: 'EXPENSE',
          deletedAt: null,
          transactionDate: {
            gte: startOfMonth,
            lte: endOfMonth,
          },
        },
      });
      utilityExpenses = utilitySpentAgg._sum.amount ? Number(utilitySpentAgg._sum.amount) : 0;
    }

    if (utilityExpenses > 0) {
      tips.push({
        title: 'Energy Efficiency',
        message: `Your utility spending is ${symbol}${Math.round(utilityExpenses)} this month. Adjusting thermostats or lights could save you up to 10%.`,
        color: '#F59E0B',
        impact: 'Low Impact',
      });
    }

    const generalTips = [
      {
        title: 'Track Every Expense',
        message: 'Logging even the smallest cash transactions helps pinpoint where your money goes.',
        color: '#64748B',
        impact: 'Low Impact',
      },
      {
        title: '50/30/20 Rule',
        message: 'Try budgeting 50% of income for needs, 30% for wants, and 20% for savings.',
        color: '#8B5CF6',
        impact: 'High Impact',
      },
      {
        title: 'Build Emergency Fund',
        message: 'Aim to save 3-6 months of living expenses for unexpected situations.',
        color: '#EF4444',
        impact: 'High Impact',
      }
    ];

    for (const gt of generalTips) {
      if (tips.length >= 3) break;
      if (!tips.some(t => t.title === gt.title)) {
        tips.push(gt);
      }
    }

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
      alerts,
      tips,
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
