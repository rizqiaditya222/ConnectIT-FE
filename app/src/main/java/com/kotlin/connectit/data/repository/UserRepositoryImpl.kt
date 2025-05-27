package com.kotlin.connectit.data.repository

import com.kotlin.connectit.data.api.ApiService
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.data.api.request.UpdateUsernameRequest
import com.kotlin.connectit.data.api.response.GetAllUsersResponse
import com.kotlin.connectit.data.api.response.GetUserByIdResponse
import com.kotlin.connectit.data.api.response.UpdateUserResponse
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit.util.safeApiCall
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getAllUsers(): ResultWrapper<GetAllUsersResponse> {
        return safeApiCall { apiService.getAllUsers() }
    }

    override suspend fun updateAuthenticatedUser(updateUsernameRequest: UpdateUsernameRequest): ResultWrapper<UpdateUserResponse> {
        val token = TokenManager.getToken()
        if (token.isNullOrBlank()) {
            return ResultWrapper.Error(message = "Akses ditolak: Token tidak ditemukan untuk update pengguna.", code = 401)
        }
        val authHeader = "$token"
        return safeApiCall { apiService.updateAuthenticatedUser(authHeader, updateUsernameRequest) }
    }

    override suspend fun getUserById(userId: String): ResultWrapper<GetUserByIdResponse> {
        return safeApiCall { apiService.getUserById(userId) }
    }
}