package edu.bluejack24_2.ecoai.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val passwordHash: String = "",
    val profilePictureUrl: String = "",
    val username: String = ""
)
