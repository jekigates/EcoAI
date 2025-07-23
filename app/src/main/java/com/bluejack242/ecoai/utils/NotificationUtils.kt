package com.bluejack242.ecoai.utils

import com.bluejack242.ecoai.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

fun sendNotificationWithType(
    fromUserId: String,
    toUserId: String,
    type: String,
    postId: String? = null
) {
    if (fromUserId == toUserId) return

    val notification = Notification(
        fromUserId = fromUserId,
        toUserId = toUserId,
        postId = postId ?: "",
        type = type,
        createdAt = Timestamp.now()
    )

    Firebase.firestore.collection("notifications")
        .add(notification)
}

