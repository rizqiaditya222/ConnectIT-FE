package com.kotlin.connectit.data.api.response

data class DeletePostData(
    val message: String
)

data class DeletePostResponse(
    val data: DeletePostData?,
    val status: String
)