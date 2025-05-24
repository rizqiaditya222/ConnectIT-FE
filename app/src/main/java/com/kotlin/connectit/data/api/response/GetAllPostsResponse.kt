package com.kotlin.connectit.data.api.response

import com.google.gson.annotations.SerializedName

data class PostItem(
    val caption: String?,
    @SerializedName("created_at")
    val createdAt: String,
    val id: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: String
)

data class GetAllPostsResponse(
    val data: List<PostItem>?,
    val status: String
)