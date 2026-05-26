package com.example.moneymap.data.session

import android.content.Context
import com.example.moneymap.data.model.UserDto

class AuthSession(context: Context) {
    private val prefs = context.getSharedPreferences("moneymap_auth", Context.MODE_PRIVATE)

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)

    val userName: String?
        get() = prefs.getString(KEY_NAME, null)

    val userEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)

    val userRole: String?
        get() = prefs.getString(KEY_ROLE, null)

    val userCurrency: String?
        get() = prefs.getString(KEY_CURRENCY, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun saveUser(user: UserDto) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_NAME, user.name)
            .putString(KEY_ROLE, user.role)
            .putString(KEY_CURRENCY, user.currency)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_ROLE = "role"
        private const val KEY_CURRENCY = "currency"
    }
}
