package com.kotlin.connectit.data.repository

import com.kotlin.connectit.data.api.ApiService
import com.kotlin.connectit.data.api.response.GetAllPostsResponse
import com.kotlin.connectit.data.api.response.UserData
import com.kotlin.connectit.domain.repository.SearchRepository
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit.util.safeApiCall
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SearchRepository {

    override suspend fun searchPosts(searchQuery: String): ResultWrapper<List<GetAllPostsResponse>> {
        return safeApiCall { apiService.searchPosts(searchQuery) }
    }

    override suspend fun searchUsers(searchQuery: String): ResultWrapper<List<UserData>> {
        return safeApiCall { apiService.searchUsers(searchQuery) }
    }
}