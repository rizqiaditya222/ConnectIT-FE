package com.kotlin.connectit.data.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

object TokenManager {
    private const val PREFS_NAME = "connectit_prefs"
    private const val KEY_TOKEN = "auth_token"

    private var sharedPreferences: SharedPreferences? = null
    private var currentToken: String? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentToken = sharedPreferences?.getString(KEY_TOKEN, null)
    }

    fun saveToken(token: String?) {
        currentToken = token
        sharedPreferences?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? {
        if (currentToken == null) { // Jika belum ada di cache memori, coba muat dari SharedPreferences
            currentToken = sharedPreferences?.getString(KEY_TOKEN, null)
        }
        return currentToken
    }

    fun clearToken() {
        currentToken = null
        sharedPreferences?.edit()?.remove(KEY_TOKEN)?.apply()
    }
}

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getToken()
        val originalRequest = chain.request()

        if (!token.isNullOrBlank()) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", token)
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(originalRequest)
    }
}