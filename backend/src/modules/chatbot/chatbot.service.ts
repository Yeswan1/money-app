import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { ConfigService } from '@nestjs/config';

type FinancialContext = {
  user: {
    name: string;
    role: string;
    currency: string;
  };
  monthLabel: string;
  monthlyIncome: number;
  monthlySpent: number;
  weeklySpent: number;
  netSavings: number;
  spendByCategory: Record<string, number>;
  budgets: Array<{
    category: string;
    limit: number;
    spent: number;
    remaining: number;
    utilizationPercent: number;
  }>;
  goals: Array<{
    name: string;
    target: number;
    current: number;
    remaining: number;
    progressPercent: number;
    targetDate: Date | null;
  }>;
  recentTransactions: Array<{
    description: string;
    category: string;
    type: string;
    amount: number;
    date: string;
  }>;
};

@Injectable()
export class ChatbotService {
  constructor(
    private prisma: PrismaService,
    private configService: ConfigService,
  ) {}

  async getSessions(userId: string) {
    return this.prisma.chatSession.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      include: {
        messages: {
          orderBy: { createdAt: 'desc' },
          take: 1,
        },
      },
    });
  }

  async getMessages(userId: string, sessionId: string) {
    const session = await this.prisma.chatSession.findFirst({
      where: { id: sessionId, userId },
    });

    if (!session) {
      throw new NotFoundException('Chat session not found');
    }

    return this.prisma.chatMessage.findMany({
      where: { sessionId },
      orderBy: { createdAt: 'asc' },
    });
  }

  async sendMessage(userId: string, sessionId: string | undefined, content: string) {
    console.log(`[ChatbotService] sendMessage: userId=${userId}, sessionId=${sessionId}, content="${content}"`);
    const cleanContent = content.trim();
    if (!cleanContent) {
      throw new NotFoundException('Message content cannot be empty');
    }

    const activeSessionId = await this.resolveSession(userId, sessionId);

    await this.prisma.chatMessage.create({
      data: {
        sessionId: activeSessionId,
        content: cleanContent,
        isUser: true,
      },
    });

    const financialContext = await this.buildFinancialContext(userId);
    console.log(`[ChatbotService] financialContext: income=${financialContext.monthlyIncome}, spent=${financialContext.monthlySpent}, budgetsCount=${financialContext.budgets.length}, goalsCount=${financialContext.goals.length}`);

    const reply = this.processRuleBasedQuery(cleanContent, financialContext);
    console.log(`[ChatbotService] reply: "${reply.replace(/\n/g, ' ')}"`);

    const botMessage = await this.prisma.chatMessage.create({
      data: {
        sessionId: activeSessionId,
        content: reply,
        isUser: false,
      },
    });

    return {
      sessionId: activeSessionId,
      message: botMessage,
    };
  }

  private async resolveSession(userId: string, sessionId?: string) {
    if (!sessionId) {
      const session = await this.prisma.chatSession.create({
        data: { userId },
      });
      return session.id;
    }

    const session = await this.prisma.chatSession.findFirst({
      where: { id: sessionId, userId },
    });

    if (!session) {
      throw new NotFoundException('Chat session not found');
    }

    return session.id;
  }

  private async buildFinancialContext(userId: string): Promise<FinancialContext> {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth(); // 0-indexed: 6 for July
    const startOfMonth = new Date(Date.UTC(year, month, 1, 0, 0, 0, 0));
    const endOfMonth = new Date(Date.UTC(year, month + 1, 0, 23, 59, 59, 999));
    const sevenDaysAgo = new Date(Date.UTC(today.getFullYear(), today.getMonth(), today.getDate() - 7, 0, 0, 0, 0));
    const monthLabel = today.toLocaleString('en-US', {
      month: 'long',
      year: 'numeric',
    });

    const [user, transactionAgg, expenses, budgets, goals, recentTransactions, weeklySpentAgg] =
      await Promise.all([
        this.prisma.user.findUnique({
          where: { id: userId, deletedAt: null },
          select: { name: true, role: true, currency: true },
        }),
        this.prisma.transaction.groupBy({
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
        }),
        this.prisma.transaction.findMany({
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
        }),
        this.prisma.budget.findMany({
          where: { userId, month: month + 1, year },
          include: { category: true },
        }),
        this.prisma.savingsGoal.findMany({
          where: { userId, isCompleted: false },
          orderBy: { createdAt: 'desc' },
          take: 5,
        }),
        this.prisma.transaction.findMany({
          where: { userId, deletedAt: null },
          include: { category: true },
          orderBy: { transactionDate: 'desc' },
          take: 8,
        }),
        this.prisma.transaction.aggregate({
          _sum: { amount: true },
          where: {
            userId,
            type: 'EXPENSE',
            deletedAt: null,
            transactionDate: {
              gte: sevenDaysAgo,
            },
          },
        }),
      ]);

    let monthlyIncome = 0;
    let monthlySpent = 0;
    for (const group of transactionAgg) {
      if (group.type === 'INCOME') {
        monthlyIncome = Number(group._sum.amount || 0);
      }
      if (group.type === 'EXPENSE') {
        monthlySpent = Number(group._sum.amount || 0);
      }
    }

    const weeklySpent = Number(weeklySpentAgg._sum.amount || 0);

    const spendByCategory: Record<string, number> = {};
    for (const expense of expenses) {
      spendByCategory[expense.category.name] =
        (spendByCategory[expense.category.name] || 0) + Number(expense.amount);
    }

    const budgetContext = budgets.map((budget) => {
      const spent = spendByCategory[budget.category.name] || 0;
      const limit = Number(budget.amount);
      return {
        category: budget.category.name,
        limit,
        spent,
        remaining: Math.max(limit - spent, 0),
        utilizationPercent: limit > 0 ? Math.round((spent / limit) * 100) : 0,
      };
    });

    return {
      user: {
        name: user?.name || 'User',
        role: user?.role || 'PERSONAL',
        currency: user?.currency || 'INR',
      },
      monthLabel,
      monthlyIncome,
      monthlySpent,
      weeklySpent,
      netSavings: monthlyIncome - monthlySpent,
      spendByCategory,
      budgets: budgetContext,
      goals: goals.map((goal) => {
        const target = Number(goal.targetAmount);
        const current = Number(goal.currentAmount);
        return {
          name: goal.name,
          target,
          current,
          remaining: Math.max(target - current, 0),
          progressPercent: target > 0 ? Math.round((current / target) * 100) : 0,
          targetDate: goal.targetDate,
        };
      }),
      recentTransactions: recentTransactions.map((transaction) => ({
        description: transaction.description || transaction.category.name,
        category: transaction.category.name,
        type: transaction.type,
        amount: Number(transaction.amount),
        date: transaction.transactionDate.toISOString().slice(0, 10),
      })),
    };
  }

  private processRuleBasedQuery(message: string, context: FinancialContext): string {
    const cleanMessage = message.trim();

    if (/\b(budget|limit|allowance)s?\b/i.test(cleanMessage)) {
      return this.handleBudget(context);
    }
    if (/\b(save|saving|goal)s?\b/i.test(cleanMessage)) {
      return this.handleSavings(context);
    }
    if (/\b(spend|spent|spending|expense|expensse|expence|outgo|cost|payment)s?\b/i.test(cleanMessage)) {
      return this.handleSpending(context, cleanMessage);
    }
    if (/\b(recent|history|transaction|record)s?\b/i.test(cleanMessage)) {
      return this.handleTransactions(context);
    }
    if (/\b(advice|tip|suggest)s?\b/i.test(cleanMessage)) {
      return this.handleAdvice(context);
    }
    if (/\b(hi|hello|hey|help|how to)\b/i.test(cleanMessage)) {
      return this.handleGreeting(context);
    }

    return this.handleFallback(cleanMessage, context);
  }

  private handleGreeting(context: FinancialContext): string {
    const name = context.user.name;
    const role = context.user.role;

    return `👋 Hello, ${name}! Welcome back to MoneyMap.

As a ${role} user, here are some quick tips on what you can ask me:
- 📊 "Show my budget" to see your monthly budget limits and progress.
- 💸 "How much have I spent?" for a breakdown of your top expenses.
- 🎯 "What are my savings goals?" to check progress and estimated weekly savings targets.
- 📋 "Show recent transactions" to see your recent transactions.
- 💡 "Give me advice" for personalized financial tips tailored to your profile.

How can I help you manage your finances today?`;
  }

  private handleBudget(context: FinancialContext): string {
    const currency = context.user.currency;
    if (!context.budgets.length) {
      return `📊 Monthly Budget Overview (${context.monthLabel})

You do not have any budgets set for this month yet. 
Setting a budget helps you control expenses. We recommend setting limits on top categories like:
- 🍔 Food
- 🛒 Shopping
- 🔌 Bills

Open the Budget Planner in the app to set your first limit!`;
    }

    let totalBudget = 0;
    let totalSpent = 0;
    const budgetList: string[] = [];

    for (const budget of context.budgets) {
      totalBudget += budget.limit;
      totalSpent += budget.spent;

      let statusEmoji = '✅ Controlled';
      if (budget.utilizationPercent >= 80) {
        statusEmoji = '⚠️ High Utilization';
      } else if (budget.utilizationPercent >= 50) {
        statusEmoji = '📊 Moderate';
      }

      budgetList.push(
        `- ${budget.category}: ${budget.spent.toFixed(2)} / ${budget.limit.toFixed(2)} ${currency} (${budget.utilizationPercent}%) - ${statusEmoji}`
      );
    }

    const overallPercent = totalBudget > 0 ? Math.round((totalSpent / totalBudget) * 100) : 0;

    return `📊 Monthly Budget Overview (${context.monthLabel})

Summary:
- Total Budgeted: ${totalBudget.toFixed(2)} ${currency}
- Total Spent: ${totalSpent.toFixed(2)} ${currency}
- Overall Utilization: ${overallPercent}%

Category Breakdown:
${budgetList.join('\n')}

${overallPercent >= 80 ? '⚠️ Warning: You have used up a significant portion of your total budget. Consider trimming variable expenses.' : '✅ Your overall spending is currently within the safe range. Keep it up!'}`;
  }

  private handleSavings(context: FinancialContext): string {
    const currency = context.user.currency;
    if (!context.goals.length) {
      return `🎯 Savings Goals

You do not have any active savings goals right now. 
Creating a savings goal helps you stay motivated. We recommend starting with a goal like:
- 🏦 Emergency Fund (3-6 months of expenses)
- ✈️ Travel Fund
- 🎓 Education/Gadgets

Open the Savings Goals screen to set one up!`;
    }

    const goalSummaries: string[] = [];
    const today = new Date();

    for (const goal of context.goals) {
      let targetAdvice = '';
      if (goal.targetDate) {
        const targetDateObj = new Date(goal.targetDate);
        const diffTime = targetDateObj.getTime() - today.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const weeks = Math.ceil(diffDays / 7);

        if (weeks > 0 && goal.remaining > 0) {
          const weeklyAmount = (goal.remaining / weeks).toFixed(2);
          const formattedDate = targetDateObj.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
          });
          targetAdvice = `To reach your goal '${goal.name}' by ${formattedDate}, try saving ${weeklyAmount} ${currency} weekly (${weeks} weeks remaining).`;
        } else if (goal.remaining > 0) {
          const weeklyAmount = (goal.remaining / 4).toFixed(2);
          targetAdvice = `Your target date has passed or is today. To complete '${goal.name}' soon, try saving ${weeklyAmount} ${currency} weekly over the next 4 weeks.`;
        } else {
          targetAdvice = `Congratulations! You have fully reached your target amount for '${goal.name}'.`;
        }
      } else {
        const weeklyAmount = (goal.remaining / 12).toFixed(2);
        targetAdvice = `No target date is set for '${goal.name}'. If you save ${weeklyAmount} ${currency} weekly, you will achieve this goal in 12 weeks.`;
      }

      goalSummaries.push(
        `- ${goal.name}: ${goal.current.toFixed(2)} / ${goal.target.toFixed(2)} ${currency} (${goal.progressPercent}% complete)\n  Advice: ${targetAdvice}`
      );
    }

    return `🎯 Savings & Goals Tracker

Here are your active savings goals:

${goalSummaries.join('\n\n')}

Pro Tip: Small, consistent weekly deposits are the easiest way to hit large savings milestones.`;
  }

  private handleSpending(context: FinancialContext, message: string): string {
    const currency = context.user.currency;
    const cleanMessage = message.toLowerCase();

    // 1. Check if the user asked about weekly spending specifically
    if (cleanMessage.includes('week')) {
      return `💸 Weekly Spending Analysis

You have spent a total of ${context.weeklySpent.toFixed(2)} ${currency} over the last 7 days.`;
    }

    // 2. Check if the user mentioned a specific category name
    const categoriesWithSpend = Object.keys(context.spendByCategory);
    const budgetCategories = context.budgets.map((b) => b.category);
    // Combine all unique categories
    const allCategories = Array.from(new Set([...categoriesWithSpend, ...budgetCategories]));
    const mentionedCategory = allCategories.find((cat) => cleanMessage.includes(cat.toLowerCase()));

    if (mentionedCategory) {
      const amount = context.spendByCategory[mentionedCategory] || 0;
      return `💸 Spending on ${mentionedCategory} (${context.monthLabel})

You have spent a total of ${amount.toFixed(2)} ${currency} on ${mentionedCategory} this month.`;
    }

    // 3. Fallback to default monthly spending breakdown
    if (context.monthlySpent === 0) {
      return `💸 Spending Analysis (${context.monthLabel})

You haven't logged any expenses for this month yet. 
Try logging daily purchases to get an accurate view of where your money goes.`;
    }

    const categoryEntries = Object.entries(context.spendByCategory).sort((a, b) => b[1] - a[1]);
    if (categoryEntries.length === 0) {
      return `💸 Spending Analysis (${context.monthLabel})

Total Spent: ${context.monthlySpent.toFixed(2)} ${currency}
No category breakdown is available yet. Please categorize your transactions.`;
    }

    const topCategoryName = categoryEntries[0][0];
    const topCategoryAmount = categoryEntries[0][1];
    const topCategoryPercent = Math.round((topCategoryAmount / context.monthlySpent) * 100);

    const top3List: string[] = [];
    const limit = Math.min(categoryEntries.length, 3);
    for (let i = 0; i < limit; i++) {
      const [cat, amt] = categoryEntries[i];
      const pct = Math.round((amt / context.monthlySpent) * 100);
      top3List.push(`${i + 1}. ${cat}: ${amt.toFixed(2)} ${currency} (${pct}%)`);
    }

    return `💸 Spending Analysis (${context.monthLabel})

- Total Spending: ${context.monthlySpent.toFixed(2)} ${currency}
- Top Expense Category: ${topCategoryName} (${topCategoryAmount.toFixed(2)} ${currency}, representing ${topCategoryPercent}% of your total monthly spending)

Top Expense Breakdown:
${top3List.join('\n')}

Tip: Consider setting a budget limit specifically for ${topCategoryName} to control overall spending.`;
  }

  private handleTransactions(context: FinancialContext): string {
    const currency = context.user.currency;
    if (!context.recentTransactions.length) {
      return `📋 Recent Transactions

No transactions found. Open the transactions tab to log your income or expenses.`;
    }

    const latestTransactions = context.recentTransactions.slice(0, 5);
    const transactionRows = latestTransactions.map((tx) => {
      const sign = tx.type === 'INCOME' ? '➕' : '➖';
      return `- [${tx.date}] ${tx.description} (${tx.category}) ${sign} ${tx.amount.toFixed(2)} ${currency}`;
    });

    return `📋 Recent Transactions

Here are your last 5 logged transactions:

${transactionRows.join('\n')}

Verify these details. If any entry looks incorrect, you can edit it directly in the Transactions tab.`;
  }

  private handleAdvice(context: FinancialContext): string {
    const role = context.user.role;
    const currency = context.user.currency;

    let adviceText = '';
    switch (role) {
      case 'STUDENT':
        adviceText = `🎓 Student Financial Tips:
1. Allowance Tracking: Keep a close eye on your allowance. Aim to budget at least 10% of it for emergencies or books.
2. Limit Impulse Buying: Food & dining and social shopping are the biggest spending drivers for students. Try to set weekly limits on those.
3. Small Steps: Even saving just 500 ${currency} a month makes a difference. Try using a digital jar/goal for small deposits.`;
        break;

      case 'PROFESSIONAL':
        adviceText = `💼 Professional Wealth Building:
1. Target a 20% Savings Rate: Automate transfers of at least 20% of your monthly income directly into savings or investments.
2. Review Subscriptions: Check your active monthly subscriptions. Cancel any that haven't been used in the last 30 days.
3. Budget Compliance: Keep your budget utilization below 80% on core variable categories to avoid paycheck-to-paycheck cycles.`;
        break;

      case 'HOMEMAKER':
        adviceText = `🏡 Household Finance Optimization:
1. Track Utilities & Groceries: These categories form the foundation of household expenses. Compare monthly bills to identify optimization opportunities.
2. Bulk Purchases: For household supplies with long shelf life, bulk purchasing can save up to 15% annually.
3. Emergency Cushion: Ensure you have an active savings goal designated as a household emergency fund covering at least 3 months of utility bills.`;
        break;

      default:
      case 'PERSONAL':
        const income = context.monthlyIncome;
        const spent = context.monthlySpent;
        const net = context.netSavings;

        let healthStatus = '';
        if (income === 0) {
          healthStatus = `You haven't logged any income yet. Please record your income to enable full cash flow diagnostics.`;
        } else {
          const savingsRate = Math.round((net / income) * 100);
          if (savingsRate < 0) {
            healthStatus = `⚠️ Alert: You spent more than you earned this month by ${Math.abs(net).toFixed(2)} ${currency}. Look closely at your transaction log to trim non-essential spending.`;
          } else if (savingsRate < 10) {
            healthStatus = `📊 Caution: Your current savings rate is ${savingsRate}%. Try to increase this to 15% or 20% by cutting down on luxury categories.`;
          } else {
            healthStatus = `✅ Great job! Your savings rate is ${savingsRate}% (${net.toFixed(2)} ${currency} saved). You are on track to build strong financial stability.`;
          }
        }

        adviceText = `👤 Personal Cash Flow Diagnostics:
${healthStatus}

General Rules of Thumb:
- Aim to keep fixed costs (rent, utilities) below 50% of your income.
- Set aside at least 15% of your income for long-term goals.
- Build an emergency fund of 3-6 months' worth of living expenses.`;
        break;
    }

    return `💡 Personalized Advice & Insights

${adviceText}`;
  }

  private handleFallback(userMessage: string, context: FinancialContext): string {
    return `🤔 I'm not sure how to respond to that. As a local, deterministic assistant, I work best when you ask about specific financial features.

Please try one of the following queries or commands:
- 📊 "Show my budget" / "Am I within my limit?"
- 🎯 "What are my savings goals?" / "How much should I save?"
- 💸 "Show my spending" / "Where did my money go?"
- 📋 "Show recent transactions" / "List recent activity"
- 💡 "Give me advice" / "Financial tips"
- 👋 "Hello" / "Help"`;
  }
}
