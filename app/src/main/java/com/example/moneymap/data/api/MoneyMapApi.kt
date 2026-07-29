package com.example.moneymap.data.api

import com.example.moneymap.data.model.ApiEnvelope
import com.example.moneymap.data.model.CategoryDto
import com.example.moneymap.data.model.CreateSavingsGoalRequest
import com.example.moneymap.data.model.CreateSubscriptionRequest
import com.example.moneymap.data.model.CreateTransactionRequest
import com.example.moneymap.data.model.DashboardStatsResponse
import com.example.moneymap.data.model.GoogleSignInRequest
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
import com.example.moneymap.data.model.TransactionDto
import com.example.moneymap.data.model.UpdateSettingsRequest
import com.example.moneymap.data.model.UpdateProfileRequest
import com.example.moneymap.data.model.UserProfileResponse
import com.example.moneymap.data.model.UserDto
import com.example.moneymap.data.model.ForgotPasswordRequest
import com.example.moneymap.data.model.ForgotPasswordResponse
import com.example.moneymap.data.model.ResetPasswordRequest
import com.example.moneymap.data.model.WeeklyReportResponse
import com.example.moneymap.data.model.MonthlyReportResponse
import com.example.moneymap.data.model.SpendingTrendDto
import com.example.moneymap.data.model.BudgetDto
import com.example.moneymap.data.model.BudgetSummaryResponse
import com.example.moneymap.data.model.CreateCategoryRequest
import com.example.moneymap.data.model.UpdateSavingsGoalRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface MoneyMapApi {
    @POST("auth/signup")
    suspend fun signup(@Body request: RegisterRequest): ApiEnvelope<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiEnvelope<LoginResponse>

    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): ApiEnvelope<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): ApiEnvelope<LoginResponse>

    @GET("categories")
    suspend fun getCategories(): ApiEnvelope<List<CategoryDto>>

    @GET("users/profile")
    suspend fun getProfile(): ApiEnvelope<UserProfileResponse>

    @PATCH("users/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): ApiEnvelope<UserProfileResponse>

    @PATCH("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiEnvelope<UserProfileResponse>

    @GET("reports/dashboard")
    suspend fun getDashboardStats(): ApiEnvelope<DashboardStatsResponse>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("type") type: String? = null,
        @Query("tag") tag: String? = null
    ): ApiEnvelope<TransactionsListResponse>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): ApiEnvelope<TransactionCreateResponse>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") id: String): ApiEnvelope<TransactionDto>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): ApiEnvelope<Any>

    @PATCH("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body request: CreateTransactionRequest
    ): ApiEnvelope<TransactionDto>

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

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiEnvelope<ForgotPasswordResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiEnvelope<Any>

    @GET("reports/weekly")
    suspend fun getWeeklyReport(): ApiEnvelope<WeeklyReportResponse>

    @GET("reports/monthly")
    suspend fun getMonthlyReport(): ApiEnvelope<MonthlyReportResponse>

    @GET("reports/trends")
    suspend fun getSpendingTrends(): ApiEnvelope<List<SpendingTrendDto>>

    @GET("budgets")
    suspend fun getBudgets(
        @retrofit2.http.Query("month") month: Int,
        @retrofit2.http.Query("year") year: Int
    ): ApiEnvelope<List<BudgetDto>>

    @GET("budgets/summary")
    suspend fun getBudgetSummary(
        @retrofit2.http.Query("month") month: Int,
        @retrofit2.http.Query("year") year: Int
    ): ApiEnvelope<BudgetSummaryResponse>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): ApiEnvelope<CategoryDto>

    @retrofit2.http.PATCH("savings-goals/{id}")
    suspend fun updateSavingsGoal(
        @retrofit2.http.Path("id") id: String,
        @Body request: UpdateSavingsGoalRequest
    ): ApiEnvelope<SavingsGoalDto>

    @retrofit2.http.DELETE("savings-goals/{id}")
    suspend fun deleteSavingsGoal(@retrofit2.http.Path("id") id: String): ApiEnvelope<Any>
}
