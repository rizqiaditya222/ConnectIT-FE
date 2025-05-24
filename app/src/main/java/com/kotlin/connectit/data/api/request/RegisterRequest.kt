package com.kotlin.connectit.data.api.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)
