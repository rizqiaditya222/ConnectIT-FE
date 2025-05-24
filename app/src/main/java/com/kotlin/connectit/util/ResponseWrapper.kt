package com.kotlin.connectit.util

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>()
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val errorBody: String? = null,
        val exception: Exception? = null
    ) : ResultWrapper<Nothing>()
}