package com.bluejack242.ecoai.model

data class ResetPasswordRequest(
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = ""
)
