package com.kotlin.connectit.data.api.response

data class GetUserByIdResponse(
    val data: UserData?,
    val status: String
)