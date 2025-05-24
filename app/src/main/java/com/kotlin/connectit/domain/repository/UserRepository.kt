package com.kotlin.connectit.domain.repository

import com.kotlin.connectit.data.api.request.UpdateUsernameRequest
import com.kotlin.connectit.data.api.response.GetAllUsersResponse
import com.kotlin.connectit.data.api.response.GetUserByIdResponse
import com.kotlin.connectit.data.api.response.UpdateUserResponse
import com.kotlin.connectit.util.ResultWrapper

interface UserRepository {
    suspend fun getAllUsers(): ResultWrapper<GetAllUsersResponse>
    suspend fun updateAuthenticatedUser(updateUsernameRequest: UpdateUsernameRequest): ResultWrapper<UpdateUserResponse>
    suspend fun getUserById(userId: String): ResultWrapper<GetUserByIdResponse>
}