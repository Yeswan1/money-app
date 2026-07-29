package com.example.moneymap.data.model

data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T?
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "PERSONAL",
    val currency: String = "USD"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class GoogleSignInRequest(
    val idToken: String
)

data class LoginResponse(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val currency: String
)

data class CategoryDto(
    val id: String,
    val name: String,
    val color: String,
    val icon: String,
    val isSystem: Boolean
)

data class CreateTransactionRequest(
    val categoryId: String,
    val amount: Double,
    val type: String = "EXPENSE",
    val description: String? = null,
    val transactionDate: String,
    val tags: List<String>? = null
)

data class TransactionCreateResponse(
    val transaction: TransactionDto?,
    val budgetWarning: Boolean,
    val budgetLimit: Double,
    val currentSpending: Double
)

data class TransactionDto(
    val id: String,
    val amount: Double,
    val type: String,
    val description: String?,
    val transactionDate: String,
    val category: CategoryDto? = null,
    val tags: List<String>? = null
)

data class TransactionsListResponse(
    val total: Int,
    val transactions: List<TransactionDto>
)

data class SetBudgetRequest(
    val categoryId: String,
    val amount: Double,
    val month: Int,
    val year: Int
)

data class CreateSavingsGoalRequest(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: String? = null,
    val icon: String = "flag",
    val color: String = "#3B82F6"
)

data class SavingsGoalDto(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String?,
    val icon: String? = null,
    val color: String? = null
)

data class SendChatMessageRequest(
    val sessionId: String? = null,
    val content: String
)

data class ChatbotMessageDto(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val createdAt: String
)

data class SendChatMessageResponse(
    val sessionId: String,
    val message: ChatbotMessageDto
)

data class CreateSubscriptionRequest(
    val name: String,
    val amount: Double,
    val billingCycle: String = "MONTHLY",
    val nextBillingDate: String,
    val color: String = "#3B82F6",
    val isActive: Boolean = true
)

data class SubscriptionDto(
    val id: String,
    val name: String,
    val amount: Double,
    val billingCycle: String,
    val nextBillingDate: String,
    val color: String,
    val isActive: Boolean
)

data class UserProfileResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val currency: String,
    val profile: UserProfileDetails? = null,
    val notificationPref: NotificationPreferenceDto? = null
)

data class UserProfileDetails(
    val onboardingCompleted: Boolean? = null,
    val notificationsEnabled: Boolean? = null,
    val monthlyBudget: Double? = null,
    val monthlyIncome: Double? = null,
    val monthlyAllowance: Double? = null,
    val financialGoal: String? = null
)

data class NotificationPreferenceDto(
    val budgetAlerts: Boolean,
    val goalReminders: Boolean,
    val subscriptionReminders: Boolean,
    val weeklyReport: Boolean,
    val monthlyReport: Boolean
)

data class UpdateSettingsRequest(
    val name: String? = null,
    val currency: String? = null,
    val role: String? = null,
    val notificationsEnabled: Boolean? = null,
    val budgetAlerts: Boolean? = null,
    val goalReminders: Boolean? = null,
    val subscriptionReminders: Boolean? = null,
    val weeklyReport: Boolean? = null,
    val monthlyReport: Boolean? = null
)

data class UpdateProfileRequest(
    val institution: String? = null,
    val yearOfStudy: String? = null,
    val monthlyAllowance: Double? = null,
    val companyName: String? = null,
    val jobTitle: String? = null,
    val monthlyIncome: Double? = null,
    val householdSize: Int? = null,
    val monthlyBudget: Double? = null,
    val primaryCategories: List<String>? = null,
    val financialGoal: String? = null,
    val onboardingCompleted: Boolean? = null
)

data class DashboardStatsResponse(
    val monthlySpent: Double,
    val monthlyIncome: Double,
    val netSavings: Double,
    val savingsOverview: SavingsOverviewDto,
    val budgets: List<BudgetSummaryDto>,
    val recentTransactions: List<DashboardTransactionDto>,
    val alerts: List<BudgetAlertDto> = emptyList(),
    val tips: List<SavingTipDto> = emptyList()
)

data class BudgetAlertDto(
    val title: String,
    val message: String,
    val isCritical: Boolean,
    val time: String
)

data class SavingTipDto(
    val title: String,
    val message: String,
    val color: String,
    val impact: String
)

data class SavingsOverviewDto(
    val totalTarget: Double,
    val totalCurrent: Double,
    val progressPercentage: Double
)

data class BudgetSummaryDto(
    val categoryName: String,
    val color: String,
    val limit: Double,
    val spent: Double,
    val utilizationPercentage: Double
)

data class DashboardTransactionDto(
    val id: String,
    val amount: Double,
    val type: String,
    val description: String?,
    val transactionDate: String,
    val category: String,
    val color: String,
    val icon: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class WeeklyReportResponse(
    val totalSpent: Double,
    val averageDailySpent: Double,
    val dailyTrend: List<DailyTrendDto>,
    val breakdown: List<CategoryBreakdownDto>
)

data class DailyTrendDto(
    val day: String,
    val amount: Double
)

data class CategoryBreakdownDto(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val color: String,
    val icon: String
)

data class MonthlyReportResponse(
    val totalSpent: Double,
    val previousMonthSpent: Double,
    val percentageChange: Double,
    val breakdown: List<CategoryBreakdownDto>
)

data class SpendingTrendDto(
    val monthName: String,
    val year: Int,
    val expenses: Double,
    val income: Double,
    val savings: Double
)

data class BudgetDto(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val color: String,
    val icon: String,
    val limit: Double,
    val spent: Double,
    val remaining: Double,
    val utilizationPercentage: Double
)

data class BudgetSummaryResponse(
    val totalBudgeted: Double,
    val totalSpent: Double,
    val remainingBudget: Double,
    val overallUtilization: Double,
    val breakdown: List<BudgetDto>
)

data class CreateCategoryRequest(
    val name: String,
    val color: String
)

data class UpdateSavingsGoalRequest(
    val currentAmount: Double
)
