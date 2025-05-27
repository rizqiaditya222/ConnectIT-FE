package com.kotlin.connectit.data.repository

import com.kotlin.connectit.data.api.ApiService
import com.kotlin.connectit.data.api.TokenManager // ✨ Impor TokenManager
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
        val token = TokenManager.getToken()
        if (token.isNullOrBlank()) {
            return ResultWrapper.Error(message = "Akses ditolak: Token tidak ditemukan untuk membuat post.", code = 401)
        }
        val authHeader = "$token"
        return safeApiCall { apiService.createPost(authHeader, caption, image) }
    }

    override suspend fun updatePostCaption(postId: String, updateCaptionRequest: UpdateCaptionRequest): ResultWrapper<UpdatePostResponse> {
        val token = TokenManager.getToken()
        if (token.isNullOrBlank()) {
            return ResultWrapper.Error(message = "Akses ditolak: Token tidak ditemukan untuk update post.", code = 401)
        }
        val authHeader = "$token"
        return safeApiCall { apiService.updatePostCaption(authHeader, postId, updateCaptionRequest) }
    }

    override suspend fun deletePost(postId: String): ResultWrapper<DeletePostResponse> {
        val token = TokenManager.getToken()
        if (token.isNullOrBlank()) {
            return ResultWrapper.Error(message = "Akses ditolak: Token tidak ditemukan untuk menghapus post.", code = 401)
        }
        val authHeader = "$token"
        return safeApiCall { apiService.deletePost(authHeader, postId) }
    }
}