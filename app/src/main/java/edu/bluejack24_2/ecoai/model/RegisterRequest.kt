package edu.bluejack24_2.ecoai.model

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val confirmEmail: String,
    val password: String,
    val confirmPassword: String
)
