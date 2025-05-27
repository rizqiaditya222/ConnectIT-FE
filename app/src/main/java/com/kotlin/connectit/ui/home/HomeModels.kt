package com.kotlin.connectit.ui.home

data class UiDisplayPost(
    val postId: String,
    val userId: String,
    val username: String,
    val userEmail: String,
    val caption: String?,
    val userProfileImageUrl: String?,
    val postImageUrl: String?,
    val postCreatedAt: String,
    val postUpdatedAt: String
)