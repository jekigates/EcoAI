package com.bluejack242.ecoai.utils

import com.bluejack242.ecoai.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

fun sendNotificationWithType(
    fromUserId: String,
    toUserId: String,
    type: String,
    postId: String? = null
) {
    if (fromUserId == toUserId) return
    val db = FirebaseFirestore.getInstance()

    db.collection("users").document(toUserId).get()
        .addOnSuccessListener { userDoc ->
            val notificationsEnabled = userDoc.getBoolean("notificationsEnabled") ?: true
            if (!notificationsEnabled) return@addOnSuccessListener

            val notification = Notification(
                fromUserId = fromUserId,
                toUserId = toUserId,
                postId = postId ?: "",
                type = type,
                createdAt = Timestamp.now()
            )

            db.collection("notifications")
                .add(notification)
        }
}

