package com.kotlin.connectit.data.api.response

import com.google.gson.annotations.SerializedName

data class CurrentUserResponse(
    val data: UserData?,
    val status: String
)

data class UserData(
    @SerializedName("created_at")
    val createdAt: String,
    val email: String,
    val id: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val username: String
)