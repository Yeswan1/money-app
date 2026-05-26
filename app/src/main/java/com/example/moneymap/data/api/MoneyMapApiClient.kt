package com.example.moneymap.data.api

import android.content.Context
import com.example.moneymap.BuildConfig
import com.example.moneymap.data.session.AuthSession
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MoneyMapApiClient {
    fun create(context: Context): MoneyMapApi {
        val session = AuthSession(context.applicationContext)

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = session.accessToken
                val request = if (accessToken.isNullOrBlank()) {
                    original
                } else {
                    original.newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                }
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.MONEYMAP_API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoneyMapApi::class.java)
    }
}
