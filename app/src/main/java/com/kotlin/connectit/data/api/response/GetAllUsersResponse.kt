package com.kotlin.connectit.data.api.response

data class GetAllUsersResponse(
    val data: List<UserData>?,
    val status: String
)