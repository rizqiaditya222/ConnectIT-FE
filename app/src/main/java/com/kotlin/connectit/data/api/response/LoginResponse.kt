package com.kotlin.connectit.data.api.response

data class LoginResponse(
    val data: LoginData?,
    val status: String
)

data class LoginData(
    val token: String
)