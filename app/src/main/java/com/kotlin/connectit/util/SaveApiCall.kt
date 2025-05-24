package com.kotlin.connectit.util

import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ResultWrapper<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { body ->
                ResultWrapper.Success(body)
            } ?: ResultWrapper.Error(response.code(), "Response body is null", response.errorBody()?.string())
        } else {
            ResultWrapper.Error(response.code(), response.message(), response.errorBody()?.string())
        }
    } catch (e: IOException) {
        ResultWrapper.Error(message = "Network error: ${e.localizedMessage}", exception = e)
    } catch (e: Exception) {
        ResultWrapper.Error(message = "An unexpected error occurred: ${e.localizedMessage}", exception = e)
    }
}