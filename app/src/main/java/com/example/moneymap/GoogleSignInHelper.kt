package com.example.moneymap

import android.content.Context
import com.example.moneymap.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bridges the Java LoginActivity with Kotlin suspend functions in AuthRepository.
 * Provides a simple callback interface that Java can implement.
 */
object GoogleSignInHelper {

    interface Callback {
        fun onSuccess()
        fun onError(message: String)
    }

    fun signIn(context: Context, idToken: String, callback: Callback) {
        val authRepository = AuthRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.googleSignIn(idToken)
            result.fold(
                onSuccess = {
                    callback.onSuccess()
                },
                onFailure = { throwable ->
                    callback.onError(throwable.message ?: "Unknown error")
                }
            )
        }
    }
}
