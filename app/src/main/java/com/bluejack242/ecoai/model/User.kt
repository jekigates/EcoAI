package com.bluejack242.ecoai.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val passwordHash: String = "",
    val profilePicUrl: String = "",
    val username: String = ""
)
