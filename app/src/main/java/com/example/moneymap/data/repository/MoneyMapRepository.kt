package com.example.moneymap.data.repository

import android.content.Context
import com.example.moneymap.data.api.MoneyMapApiClient
import com.example.moneymap.data.model.CategoryDto
import com.example.moneymap.data.model.CreateSavingsGoalRequest
import com.example.moneymap.data.model.CreateSubscriptionRequest
import com.example.moneymap.data.model.CreateTransactionRequest
import com.example.moneymap.data.model.DashboardStatsResponse
import com.example.moneymap.data.model.SendChatMessageRequest
import com.example.moneymap.data.model.SendChatMessageResponse
import com.example.moneymap.data.model.SetBudgetRequest
import com.example.moneymap.data.model.SavingsGoalDto
import com.example.moneymap.data.model.SubscriptionDto
import com.example.moneymap.data.model.UpdateSettingsRequest
import com.example.moneymap.data.model.UpdateProfileRequest
import com.example.moneymap.data.model.UserProfileResponse
import com.example.moneymap.data.model.TransactionCreateResponse
import com.example.moneymap.data.model.WeeklyReportResponse
import com.example.moneymap.data.model.MonthlyReportResponse
import com.example.moneymap.data.model.SpendingTrendDto
import com.example.moneymap.data.model.BudgetDto
import com.example.moneymap.data.model.BudgetSummaryResponse
import com.example.moneymap.data.model.CreateCategoryRequest
import com.example.moneymap.data.model.UpdateSavingsGoalRequest
import retrofit2.HttpException
import java.io.IOException

class MoneyMapRepository(private val context: Context) {
    private val api = MoneyMapApiClient.create(context.applicationContext)

    suspend fun getCategories(): Result<List<CategoryDto>> {
        return runCatching {
            api.getCategories().data ?: emptyList()
        }.mapError()
    }

    suspend fun getProfile(): Result<UserProfileResponse> {
        return runCatching {
            api.getProfile().data ?: error("Profile response was empty")
        }.mapError()
    }

    suspend fun updateSettings(request: UpdateSettingsRequest): Result<UserProfileResponse> {
        return runCatching {
            api.updateSettings(request).data ?: error("Profile response was empty")
        }.mapError()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileResponse> {
        return runCatching {
            api.updateProfile(request).data ?: error("Profile response was empty")
        }.mapError()
    }

    suspend fun getDashboardStats(): Result<DashboardStatsResponse> {
        return runCatching {
            val stats = api.getDashboardStats().data ?: error("Dashboard response was empty")
            com.example.moneymap.NotificationHelper.showNotificationsForAlerts(context, stats.alerts)
            stats
        }.mapError()
    }

    suspend fun getRecentTransactions(
        limit: Int = 10,
        search: String? = null,
        categoryId: String? = null,
        type: String? = null,
        tag: String? = null
    ): Result<List<com.example.moneymap.data.model.TransactionDto>> {
        return runCatching {
            api.getTransactions(
                limit = limit,
                offset = 0,
                search = search?.takeIf { it.isNotBlank() },
                categoryId = categoryId,
                type = type,
                tag = tag
            ).data?.transactions ?: emptyList()
        }.mapError()
    }

    suspend fun createTransaction(
        categoryId: String,
        amount: Double,
        type: String = "EXPENSE",
        description: String?,
        transactionDate: String,
        tags: List<String>,
    ): Result<TransactionCreateResponse> {
        return runCatching {
            api.createTransaction(
                CreateTransactionRequest(
                    categoryId = categoryId,
                    amount = amount,
                    type = type,
                    description = description?.takeIf { it.isNotBlank() },
                    transactionDate = transactionDate,
                    tags = tags.takeIf { it.isNotEmpty() },
                )
            ).data ?: error("Transaction response was empty")
        }.mapError()
    }

    suspend fun getTransaction(id: String): Result<com.example.moneymap.data.model.TransactionDto> {
        return runCatching {
            api.getTransaction(id).data ?: error("Transaction response was empty")
        }.mapError()
    }

    suspend fun deleteTransaction(id: String): Result<Unit> {
        return runCatching {
            api.deleteTransaction(id)
            Unit
        }.mapError()
    }

    suspend fun updateTransaction(
        id: String,
        categoryId: String,
        amount: Double,
        type: String,
        description: String?,
        transactionDate: String,
        tags: List<String>
    ): Result<com.example.moneymap.data.model.TransactionDto> {
        return runCatching {
            api.updateTransaction(
                id = id,
                request = CreateTransactionRequest(
                    categoryId = categoryId,
                    amount = amount,
                    type = type,
                    description = description?.takeIf { it.isNotBlank() },
                    transactionDate = transactionDate,
                    tags = tags.takeIf { it.isNotEmpty() }
                )
            ).data ?: error("Transaction response was empty")
        }.mapError()
    }

    suspend fun setBudget(categoryId: String, amount: Double, month: Int, year: Int): Result<Unit> {
        return runCatching {
            api.setBudget(
                SetBudgetRequest(
                    categoryId = categoryId,
                    amount = amount,
                    month = month,
                    year = year,
                )
            )
            Unit
        }.mapError()
    }

    suspend fun createSavingsGoal(
        name: String,
        targetAmount: Double,
        targetDate: String?,
    ): Result<Unit> {
        return runCatching {
            api.createSavingsGoal(
                CreateSavingsGoalRequest(
                    name = name,
                    targetAmount = targetAmount,
                    targetDate = targetDate?.takeIf { it.isNotBlank() },
                )
            )
            Unit
        }.mapError()
    }

    suspend fun getSavingsGoals(): Result<List<SavingsGoalDto>> {
        return runCatching {
            api.getSavingsGoals().data ?: emptyList()
        }.mapError()
    }

    suspend fun sendChatMessage(sessionId: String?, content: String): Result<SendChatMessageResponse> {
        return runCatching {
            api.sendChatMessage(
                SendChatMessageRequest(
                    sessionId = sessionId,
                    content = content,
                )
            ).data ?: error("Chat response was empty")
        }.mapError()
    }

    suspend fun getSubscriptions(): Result<List<SubscriptionDto>> {
        return runCatching {
            api.getSubscriptions().data ?: emptyList()
        }.mapError()
    }

    suspend fun createSubscription(
        name: String,
        amount: Double,
        billingCycle: String,
        nextBillingDate: String,
    ): Result<SubscriptionDto> {
        return runCatching {
            api.createSubscription(
                CreateSubscriptionRequest(
                    name = name,
                    amount = amount,
                    billingCycle = billingCycle,
                    nextBillingDate = nextBillingDate,
                )
            ).data ?: error("Subscription response was empty")
        }.mapError()
    }

    suspend fun getWeeklyReport(): Result<WeeklyReportResponse> {
        return runCatching {
            api.getWeeklyReport().data ?: error("Weekly report was empty")
        }.mapError()
    }

    suspend fun getMonthlyReport(): Result<MonthlyReportResponse> {
        return runCatching {
            api.getMonthlyReport().data ?: error("Monthly report was empty")
        }.mapError()
    }

    suspend fun getSpendingTrends(): Result<List<SpendingTrendDto>> {
        return runCatching {
            api.getSpendingTrends().data ?: error("Spending trends was empty")
        }.mapError()
    }

    suspend fun getBudgets(month: Int, year: Int): Result<List<BudgetDto>> {
        return runCatching {
            api.getBudgets(month, year).data ?: emptyList()
        }.mapError()
    }

    suspend fun getBudgetSummary(month: Int, year: Int): Result<BudgetSummaryResponse> {
        return runCatching {
            api.getBudgetSummary(month, year).data ?: error("Budget summary was empty")
        }.mapError()
    }

    suspend fun createCategory(name: String, color: String): Result<CategoryDto> {
        return runCatching {
            api.createCategory(CreateCategoryRequest(name, color)).data ?: error("Category was empty")
        }.mapError()
    }

    suspend fun updateSavingsGoal(id: String, currentAmount: Double): Result<SavingsGoalDto> {
        return runCatching {
            api.updateSavingsGoal(id, UpdateSavingsGoalRequest(currentAmount)).data ?: error("Goal was empty")
        }.mapError()
    }

    suspend fun deleteSavingsGoal(id: String): Result<Any> {
        return runCatching {
            api.deleteSavingsGoal(id).data ?: true
        }.mapError()
    }

    private fun <T> Result<T>.mapError(): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is HttpException -> Exception("Server error ${throwable.code()}: ${throwable.message()}")
                is IOException -> Exception("Cannot reach MoneyMap backend. Check that your phone and backend are on the same Wi-Fi.")
                else -> throwable
            }
        }
    }
}
