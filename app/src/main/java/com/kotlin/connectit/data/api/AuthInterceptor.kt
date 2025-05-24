package com.kotlin.connectit.data.api

import okhttp3.Interceptor
import okhttp3.Response

object TokenManager {
    private var currentToken: String? = null

    fun saveToken(token: String?) {
        currentToken = token
    }

    fun getToken(): String? {
        return currentToken
    }

    fun clearToken() {
        currentToken = null
    }
}

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getToken()
        val originalRequest = chain.request()

        if (!token.isNullOrBlank()) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(originalRequest)
    }
}