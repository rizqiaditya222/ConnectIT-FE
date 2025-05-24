package com.kotlin.connectit.data.repository

import com.kotlin.connectit.data.api.ApiService
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
        return safeApiCall { apiService.updateAuthenticatedUser(updateUsernameRequest) }
    }

    override suspend fun getUserById(userId: String): ResultWrapper<GetUserByIdResponse> {
        return safeApiCall { apiService.getUserById(userId) }
    }
}