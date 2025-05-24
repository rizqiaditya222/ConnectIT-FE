package com.kotlin.connectit.domain.repository

import com.kotlin.connectit.data.api.request.UpdateCaptionRequest
import com.kotlin.connectit.data.api.response.CreatePostResponse
import com.kotlin.connectit.data.api.response.DeletePostResponse
import com.kotlin.connectit.data.api.response.GetAllPostsResponse
import com.kotlin.connectit.data.api.response.GetPostByIdResponse
import com.kotlin.connectit.data.api.response.UpdatePostResponse
import com.kotlin.connectit.util.ResultWrapper
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface PostRepository {
    suspend fun getAllPosts(): ResultWrapper<GetAllPostsResponse>
    suspend fun getPostById(postId: String): ResultWrapper<GetPostByIdResponse>
    suspend fun getPostsByUserId(userId: String): ResultWrapper<GetAllPostsResponse>
    suspend fun createPost(caption: RequestBody, image: MultipartBody.Part?): ResultWrapper<CreatePostResponse>
    suspend fun updatePostCaption(postId: String, updateCaptionRequest: UpdateCaptionRequest): ResultWrapper<UpdatePostResponse>
    suspend fun deletePost(postId: String): ResultWrapper<DeletePostResponse>
}