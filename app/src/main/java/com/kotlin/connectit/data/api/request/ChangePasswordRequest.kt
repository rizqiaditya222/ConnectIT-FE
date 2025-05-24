package com.kotlin.connectit.data.api.request

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("new_password")
    val newPassword: String,
    @SerializedName("old_password")
    val oldPassword: String
)