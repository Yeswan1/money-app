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
import com.example.moneymap.data.model.UserProfileResponse
import com.example.moneymap.data.model.TransactionCreateResponse
import retrofit2.HttpException
import java.io.IOException

class MoneyMapRepository(context: Context) {
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

    suspend fun getDashboardStats(): Result<DashboardStatsResponse> {
        return runCatching {
            api.getDashboardStats().data ?: error("Dashboard response was empty")
        }.mapError()
    }

    suspend fun getRecentTransactions(limit: Int = 10): Result<List<com.example.moneymap.data.model.TransactionDto>> {
        return runCatching {
            api.getTransactions(limit = limit, offset = 0).data?.transactions ?: emptyList()
        }.mapError()
    }

    suspend fun createTransaction(
        categoryId: String,
        amount: Double,
        description: String?,
        transactionDate: String,
        tags: List<String>,
    ): Result<TransactionCreateResponse> {
        return runCatching {
            api.createTransaction(
                CreateTransactionRequest(
                    categoryId = categoryId,
                    amount = amount,
                    description = description?.takeIf { it.isNotBlank() },
                    transactionDate = transactionDate,
                    tags = tags.takeIf { it.isNotEmpty() },
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
