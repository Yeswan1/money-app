import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { ConfigService } from '@nestjs/config';
import OpenAI from 'openai';

type FinancialContext = {
  user: {
    name: string;
    role: string;
    currency: string;
  };
  monthLabel: string;
  monthlyIncome: number;
  monthlySpent: number;
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
  private groq: OpenAI | null = null;
  private modelName: string;

  constructor(
    private prisma: PrismaService,
    private configService: ConfigService,
  ) {
    const apiKey =
      this.configService.get<string>('GROQ_API_KEY') ||
      this.configService.get<string>('AI_API_KEY');
    const baseURL =
      this.configService.get<string>('GROQ_API_URL') ||
      this.configService.get<string>('AI_API_URL') ||
      'https://api.groq.com/openai/v1';

    this.modelName =
      this.configService.get<string>('GROQ_MODEL') ||
      this.configService.get<string>('AI_MODEL') ||
      'llama-3.3-70b-versatile';

    if (apiKey?.trim()) {
      this.groq = new OpenAI({
        apiKey,
        baseURL,
      });
    }
  }

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

    const [financialContext, recentMessages] = await Promise.all([
      this.buildFinancialContext(userId),
      this.getRecentConversation(activeSessionId),
    ]);

    const aiResponse = await this.generateAssistantReply(
      cleanContent,
      financialContext,
      recentMessages,
    );

    const botMessage = await this.prisma.chatMessage.create({
      data: {
        sessionId: activeSessionId,
        content: aiResponse,
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

  private async getRecentConversation(sessionId: string) {
    const messages = await this.prisma.chatMessage.findMany({
      where: { sessionId },
      orderBy: { createdAt: 'desc' },
      take: 8,
    });

    return messages.reverse().map((message) => ({
      role: message.isUser ? 'user' as const : 'assistant' as const,
      content: message.content,
    }));
  }

  private async buildFinancialContext(userId: string): Promise<FinancialContext> {
    const today = new Date();
    const month = today.getMonth() + 1;
    const year = today.getFullYear();
    const startOfMonth = new Date(year, month - 1, 1);
    const endOfMonth = new Date(year, month, 0);
    const monthLabel = today.toLocaleString('en-US', {
      month: 'long',
      year: 'numeric',
    });

    const [user, transactionAgg, expenses, budgets, goals, recentTransactions] =
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
          where: { userId, month, year },
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
        currency: user?.currency || 'USD',
      },
      monthLabel,
      monthlyIncome,
      monthlySpent,
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

  private async generateAssistantReply(
    userMessage: string,
    context: FinancialContext,
    recentMessages: Array<{ role: 'user' | 'assistant'; content: string }>,
  ) {
    if (!this.groq) {
      return this.generateFallbackReply(userMessage, context);
    }

    try {
      const completion = await this.groq.chat.completions.create({
        model: this.modelName,
        messages: [
          {
            role: 'system',
            content: this.buildSystemPrompt(context),
          },
          ...recentMessages.slice(0, -1),
          {
            role: 'user',
            content: userMessage,
          },
        ],
        temperature: 0.35,
        max_completion_tokens: 450,
      });

      return (
        completion.choices[0]?.message?.content?.trim() ||
        this.generateFallbackReply(userMessage, context)
      );
    } catch {
      return this.generateFallbackReply(userMessage, context);
    }
  }

  private buildSystemPrompt(context: FinancialContext) {
    return `You are MoneyMap AI, a practical personal-finance assistant inside the MoneyMap Android app.

Rules:
- Use the user's real MoneyMap data below.
- Be concise: 3 to 6 short bullet points or short paragraphs.
- Give specific next actions, not vague motivation.
- Never invent transactions, balances, budgets, or goals.
- If data is missing, say what the user should add in the app.
- Do not provide legal, tax, or investment guarantees.
- Use the user's currency code: ${context.user.currency}.

User:
${JSON.stringify(context.user)}

Financial context for ${context.monthLabel}:
${JSON.stringify(
  {
    monthlyIncome: context.monthlyIncome,
    monthlySpent: context.monthlySpent,
    netSavings: context.netSavings,
    spendByCategory: context.spendByCategory,
    budgets: context.budgets,
    savingsGoals: context.goals,
    recentTransactions: context.recentTransactions,
  },
  null,
  2,
)}`;
  }

  private generateFallbackReply(userMessage: string, context: FinancialContext) {
    const msg = userMessage.toLowerCase();
    const currency = context.user.currency;
    const categoryEntries = Object.entries(context.spendByCategory).sort((a, b) => b[1] - a[1]);
    const topCategory = categoryEntries[0];

    if (msg.includes('budget') || msg.includes('limit')) {
      if (!context.budgets.length) {
        return `You do not have budgets set for ${context.monthLabel} yet. Open Budget Planner and set limits for your biggest categories first, like Food, Bills, and Shopping.`;
      }

      const riskyBudgets = context.budgets
        .filter((budget) => budget.utilizationPercent >= 80)
        .map((budget) => `${budget.category}: ${budget.utilizationPercent}% used`)
        .join(', ');

      return riskyBudgets
        ? `Budget alert: ${riskyBudgets}. Slow down spending in those categories until next month.`
        : `Your budgets look controlled right now. Total spent this month is ${context.monthlySpent.toFixed(2)} ${currency}.`;
    }

    if (msg.includes('goal') || msg.includes('save') || msg.includes('saving')) {
      if (!context.goals.length) {
        return `You have not created savings goals yet. Create one goal with a target amount, then I can track progress and suggest weekly savings.`;
      }

      const goal = context.goals[0];
      return `${goal.name} is ${goal.progressPercent}% complete. You still need ${goal.remaining.toFixed(2)} ${currency}. If you save a fixed amount each week, this will become easier to track.`;
    }

    if (msg.includes('spend') || msg.includes('expense') || msg.includes('where')) {
      if (!topCategory) {
        return `No expenses are logged for ${context.monthLabel} yet. Add transactions first, then I can break down your spending.`;
      }

      return `You spent ${context.monthlySpent.toFixed(2)} ${currency} this month. Your highest category is ${topCategory[0]} at ${topCategory[1].toFixed(2)} ${currency}. Review recent transactions there first.`;
    }

    return `Hi ${context.user.name}, I can help with spending, budgets, savings goals, and recent transactions. This month you spent ${context.monthlySpent.toFixed(2)} ${currency} and saved ${context.netSavings.toFixed(2)} ${currency}.`;
  }
}
