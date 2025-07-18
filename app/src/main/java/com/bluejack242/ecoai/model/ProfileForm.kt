package com.bluejack242.ecoai.model

data class ProfileForm(
    val name: String,
    val username: String,
    val bio: String,
    val isUsernameTaken: Boolean
)
