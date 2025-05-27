package com.kotlin.connectit.data.repository

import com.kotlin.connectit.data.api.ApiService
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.data.api.request.ChangePasswordRequest
import com.kotlin.connectit.data.api.request.LoginRequest
import com.kotlin.connectit.data.api.request.RegisterRequest
import com.kotlin.connectit.data.api.response.CurrentUserResponse
import com.kotlin.connectit.data.api.response.LoginResponse
import com.kotlin.connectit.data.api.response.MessageResponse
import com.kotlin.connectit.data.api.response.RegisterResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit.util.safeApiCall
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun registerUser(registerRequest: RegisterRequest): ResultWrapper<RegisterResponse> {
        return safeApiCall { apiService.registerUser(registerRequest) }
    }

    override suspend fun loginUser(loginRequest: LoginRequest): ResultWrapper<LoginResponse> {
        val result = safeApiCall { apiService.loginUser(loginRequest) }

        if (result is ResultWrapper.Success) {
            result.data.data?.token?.let { token ->
                if (token.isNotBlank()) {
                    TokenManager.saveToken(token)
                }
            }
        }
        return result
    }

    override suspend fun getCurrentUser(): ResultWrapper<CurrentUserResponse> {
        val token = TokenManager.getToken()
        if (token.isNullOrBlank()) {
            return ResultWrapper.Error(message = "Akses ditolak: Token tidak ditemukan.", code = 401)
        }
        val authHeader = "$token"
        return safeApiCall { apiService.getCurrentUser(authHeader) }
    }

    override suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): ResultWrapper<MessageResponse> {
        val token = TokenManager.getToken()
        if (token.isNullOrBlank()) {
            return ResultWrapper.Error(message = "Akses ditolak: Token tidak ditemukan.", code = 401)
        }
        val authHeader = "$token"
        return safeApiCall { apiService.changePassword(authHeader, changePasswordRequest) }
    }
}