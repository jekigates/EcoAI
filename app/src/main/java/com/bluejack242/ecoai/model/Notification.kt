package com.bluejack242.ecoai.model

import com.google.firebase.Timestamp

data class Notification(
    val fromUserId: String = "",
    val toUserId: String = "",
    val postId: String = "",
    val type: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
