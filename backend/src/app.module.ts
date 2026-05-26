import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ThrottlerModule } from '@nestjs/throttler';
import { PrismaModule } from './common/prisma/prisma.module';
import { AuthModule } from './modules/auth/auth.module';
import { UsersModule } from './modules/users/users.module';
import { CategoriesModule } from './modules/categories/categories.module';
import { TransactionsModule } from './modules/transactions/transactions.module';
import { BudgetsModule } from './modules/budgets/budgets.module';
import { SavingsGoalsModule } from './modules/savings-goals/savings-goals.module';
import { SubscriptionsModule } from './modules/subscriptions/subscriptions.module';
import { ChatbotModule } from './modules/chatbot/chatbot.module';
import { ReportsModule } from './modules/reports/reports.module';

@Module({
  imports: [
    // Configure environment variables globally
    ConfigModule.forRoot({
      isGlobal: true,
    }),

    // Setup global rate limiting
    ThrottlerModule.forRoot([{
      ttl: 60000, // 60 seconds
      limit: 100, // max 100 requests per IP
    }]),

    // Core Global Modules
    PrismaModule,

    // Feature Modules
    AuthModule,
    UsersModule,
    CategoriesModule,
    TransactionsModule,
    BudgetsModule,
    SavingsGoalsModule,
    SubscriptionsModule,
    ChatbotModule,
    ReportsModule,
  ],
})
export class AppModule {}
