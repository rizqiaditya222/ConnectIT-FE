package com.kotlin.connectit.data.repository

import com.kotlin.connectit.data.api.ApiService
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
        return safeApiCall { apiService.loginUser(loginRequest) }
    }

    override suspend fun getCurrentUser(): ResultWrapper<CurrentUserResponse> {
        return safeApiCall { apiService.getCurrentUser() }
    }

    override suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): ResultWrapper<MessageResponse> {
        return safeApiCall { apiService.changePassword(changePasswordRequest) }
    }
}