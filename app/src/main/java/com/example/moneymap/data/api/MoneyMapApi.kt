package com.example.moneymap.data.api

import com.example.moneymap.data.model.ApiEnvelope
import com.example.moneymap.data.model.CategoryDto
import com.example.moneymap.data.model.CreateSavingsGoalRequest
import com.example.moneymap.data.model.CreateSubscriptionRequest
import com.example.moneymap.data.model.CreateTransactionRequest
import com.example.moneymap.data.model.DashboardStatsResponse
import com.example.moneymap.data.model.LoginRequest
import com.example.moneymap.data.model.LoginResponse
import com.example.moneymap.data.model.RefreshRequest
import com.example.moneymap.data.model.RegisterRequest
import com.example.moneymap.data.model.SavingsGoalDto
import com.example.moneymap.data.model.SendChatMessageRequest
import com.example.moneymap.data.model.SendChatMessageResponse
import com.example.moneymap.data.model.SetBudgetRequest
import com.example.moneymap.data.model.SubscriptionDto
import com.example.moneymap.data.model.TransactionCreateResponse
import com.example.moneymap.data.model.TransactionsListResponse
import com.example.moneymap.data.model.UpdateSettingsRequest
import com.example.moneymap.data.model.UserProfileResponse
import com.example.moneymap.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

interface MoneyMapApi {
    @POST("auth/signup")
    suspend fun signup(@Body request: RegisterRequest): ApiEnvelope<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiEnvelope<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): ApiEnvelope<LoginResponse>

    @GET("categories")
    suspend fun getCategories(): ApiEnvelope<List<CategoryDto>>

    @GET("users/profile")
    suspend fun getProfile(): ApiEnvelope<UserProfileResponse>

    @PATCH("users/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): ApiEnvelope<UserProfileResponse>

    @GET("reports/dashboard")
    suspend fun getDashboardStats(): ApiEnvelope<DashboardStatsResponse>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
    ): ApiEnvelope<TransactionsListResponse>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): ApiEnvelope<TransactionCreateResponse>

    @POST("budgets")
    suspend fun setBudget(@Body request: SetBudgetRequest): ApiEnvelope<Any>

    @POST("savings-goals")
    suspend fun createSavingsGoal(@Body request: CreateSavingsGoalRequest): ApiEnvelope<SavingsGoalDto>

    @GET("savings-goals")
    suspend fun getSavingsGoals(): ApiEnvelope<List<SavingsGoalDto>>

    @POST("chatbot/message")
    suspend fun sendChatMessage(@Body request: SendChatMessageRequest): ApiEnvelope<SendChatMessageResponse>

    @GET("subscriptions")
    suspend fun getSubscriptions(): ApiEnvelope<List<SubscriptionDto>>

    @POST("subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): ApiEnvelope<SubscriptionDto>
}
