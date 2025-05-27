package com.kotlin.connectit.data.api

import com.kotlin.connectit.data.api.request.ChangePasswordRequest
import com.kotlin.connectit.data.api.request.LoginRequest
import com.kotlin.connectit.data.api.request.RegisterRequest
import com.kotlin.connectit.data.api.request.UpdateCaptionRequest
import com.kotlin.connectit.data.api.request.UpdateUsernameRequest
import com.kotlin.connectit.data.api.response.CreatePostResponse
import com.kotlin.connectit.data.api.response.CurrentUserResponse
import com.kotlin.connectit.data.api.response.DeletePostResponse
import com.kotlin.connectit.data.api.response.GetAllPostsResponse
import com.kotlin.connectit.data.api.response.GetAllUsersResponse
import com.kotlin.connectit.data.api.response.GetPostByIdResponse
import com.kotlin.connectit.data.api.response.GetUserByIdResponse
import com.kotlin.connectit.data.api.response.LoginResponse
import com.kotlin.connectit.data.api.response.MessageResponse
import com.kotlin.connectit.data.api.response.RegisterResponse
import com.kotlin.connectit.data.api.response.UpdatePostResponse
import com.kotlin.connectit.data.api.response.UpdateUserResponse
import com.kotlin.connectit.data.api.response.UserData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    suspend fun registerUser(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @GET("api/auth/current-user")
    suspend fun getCurrentUser(): Response<CurrentUserResponse>

    @PUT("api/auth/change-password")
    suspend fun changePassword(
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<MessageResponse>

    @GET("api/posts")
    suspend fun getAllPosts(): Response<GetAllPostsResponse>

    @GET("api/posts/{postId}")
    suspend fun getPostById(
        @Path("postId") postId: String
    ): Response<GetPostByIdResponse>

    @GET("/api/posts/user/{userId}")
    suspend fun getPostsByUserId(
        @Path("userId") userId: String
    ): Response<GetAllPostsResponse>

    @Multipart
    @POST("api/posts")
    suspend fun createPost(
        @Part("caption") caption: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<CreatePostResponse>

    @PUT("api/posts/{postId}")
    suspend fun updatePostCaption(
        @Path("postId") postId: String,
        @Body updateCaptionRequest: UpdateCaptionRequest
    ): Response<UpdatePostResponse>

    @DELETE("api/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String 
    ): Response<DeletePostResponse>

    @GET("api/search/posts")
    suspend fun searchPosts(
        @Query("query") searchQuery: String
    ): Response<GetAllPostsResponse>

    @GET("api/search/users")
    suspend fun searchUsers(
        @Query("query") searchQuery: String
    ): Response<GetAllUsersResponse>

    @GET("api/users")
    suspend fun getAllUsers(): Response<GetAllUsersResponse>

    @PUT("api/users")
    suspend fun updateAuthenticatedUser(
        @Body updateUsernameRequest: UpdateUsernameRequest
    ): Response<UpdateUserResponse>

    @GET("api/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: String
    ): Response<GetUserByIdResponse>
}