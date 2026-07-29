package com.example.moneymap.data.repository

import android.content.Context
import com.example.moneymap.data.api.MoneyMapApiClient
import com.example.moneymap.data.model.GoogleSignInRequest
import com.example.moneymap.data.model.LoginRequest
import com.example.moneymap.data.model.RegisterRequest
import com.example.moneymap.data.model.UserDto
import com.example.moneymap.data.model.ForgotPasswordRequest
import com.example.moneymap.data.model.ForgotPasswordResponse
import com.example.moneymap.data.model.ResetPasswordRequest
import com.example.moneymap.data.session.AuthSession
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = MoneyMapApiClient.create(appContext)
    private val session = AuthSession(appContext)

    suspend fun login(email: String, password: String): Result<UserDto> {
        return runCatching {
            val response = api.login(LoginRequest(email = email.trim(), password = password))
            val data = response.data ?: error("Login response was empty")
            session.saveTokens(data.accessToken, data.refreshToken)
            session.saveUser(data.user)
            data.user
        }.mapError()
    }

    suspend fun signup(name: String, email: String, password: String): Result<UserDto> {
        return runCatching {
            api.signup(
                RegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                )
            )
            val loginResponse = api.login(LoginRequest(email = email.trim(), password = password))
            val data = loginResponse.data ?: error("Login response was empty")
            session.saveTokens(data.accessToken, data.refreshToken)
            session.saveUser(data.user)
            data.user
        }.mapError()
    }

    suspend fun googleSignIn(idToken: String): Result<UserDto> {
        return runCatching {
            val response = api.googleSignIn(GoogleSignInRequest(idToken = idToken))
            val data = response.data ?: error("Google sign-in response was empty")
            session.saveTokens(data.accessToken, data.refreshToken)
            session.saveUser(data.user)
            data.user
        }.mapError()
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return runCatching {
            val response = api.forgotPassword(ForgotPasswordRequest(email = email.trim()))
            response.data?.message ?: "If the email is registered, an OTP has been generated."
        }.mapError()
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> {
        return runCatching {
            api.resetPassword(
                ResetPasswordRequest(
                    email = email.trim(),
                    otp = otp.trim(),
                    newPassword = newPassword
                )
            )
            Unit
        }.mapError()
    }

    private fun <T> Result<T>.mapError(): Result<T> {
        return recoverCatching { throwable ->
            throw when (throwable) {
                is HttpException -> Exception("Server error ${throwable.code()}: ${throwable.message()}")
                is IOException -> Exception("Cannot reach MoneyMap backend. Start the backend and check your connection.")
                else -> throwable
            }
        }
    }
}
