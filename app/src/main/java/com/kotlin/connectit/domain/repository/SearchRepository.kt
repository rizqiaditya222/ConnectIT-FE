package com.kotlin.connectit.domain.repository

import com.kotlin.connectit.data.api.response.GetAllPostsResponse
import com.kotlin.connectit.data.api.response.UserData
import com.kotlin.connectit.util.ResultWrapper

interface SearchRepository {
    suspend fun searchPosts(searchQuery: String): ResultWrapper<List<GetAllPostsResponse>>
    suspend fun searchUsers(searchQuery: String): ResultWrapper<List<UserData>>
}