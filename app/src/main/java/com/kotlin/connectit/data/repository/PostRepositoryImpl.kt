package com.kotlin.connectit.data.repository

import com.kotlin.connectit.data.api.ApiService
import com.kotlin.connectit.data.api.request.UpdateCaptionRequest
import com.kotlin.connectit.data.api.response.CreatePostResponse
import com.kotlin.connectit.data.api.response.DeletePostResponse
import com.kotlin.connectit.data.api.response.GetAllPostsResponse
import com.kotlin.connectit.data.api.response.GetPostByIdResponse
import com.kotlin.connectit.data.api.response.UpdatePostResponse
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit.util.safeApiCall
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {

    override suspend fun getAllPosts(): ResultWrapper<GetAllPostsResponse> {
        return safeApiCall { apiService.getAllPosts() }
    }

    override suspend fun getPostById(postId: String): ResultWrapper<GetPostByIdResponse> {
        return safeApiCall { apiService.getPostById(postId) }
    }

    override suspend fun getPostsByUserId(userId: String): ResultWrapper<GetAllPostsResponse> {
        return safeApiCall { apiService.getPostsByUserId(userId) }
    }

    override suspend fun createPost(caption: RequestBody, image: MultipartBody.Part?): ResultWrapper<CreatePostResponse> {
        return safeApiCall { apiService.createPost(caption, image) }
    }

    override suspend fun updatePostCaption(postId: String, updateCaptionRequest: UpdateCaptionRequest): ResultWrapper<UpdatePostResponse> {
        return safeApiCall { apiService.updatePostCaption(postId, updateCaptionRequest) }
    }

    override suspend fun deletePost(postId: String): ResultWrapper<DeletePostResponse> {
        return safeApiCall { apiService.deletePost(postId) }
    }
}