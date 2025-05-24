package com.kotlin.connectit.data.api.response

data class RegisterResponse(
    val data: RegisterData?,
    val status: String
)

data class RegisterData(
    val message: String
)
