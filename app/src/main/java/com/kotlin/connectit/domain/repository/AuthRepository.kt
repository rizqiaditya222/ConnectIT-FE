package com.kotlin.connectit.domain.repository

import com.kotlin.connectit.data.api.request.ChangePasswordRequest
import com.kotlin.connectit.data.api.request.LoginRequest
import com.kotlin.connectit.data.api.request.RegisterRequest
import com.kotlin.connectit.data.api.response.CurrentUserResponse
import com.kotlin.connectit.data.api.response.LoginResponse
import com.kotlin.connectit.data.api.response.MessageResponse
import com.kotlin.connectit.data.api.response.RegisterResponse
import com.kotlin.connectit.util.ResultWrapper

interface AuthRepository {
    suspend fun registerUser(registerRequest: RegisterRequest): ResultWrapper<RegisterResponse>
    suspend fun loginUser(loginRequest: LoginRequest): ResultWrapper<LoginResponse>
    suspend fun getCurrentUser(): ResultWrapper<CurrentUserResponse>
    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): ResultWrapper<MessageResponse>
}