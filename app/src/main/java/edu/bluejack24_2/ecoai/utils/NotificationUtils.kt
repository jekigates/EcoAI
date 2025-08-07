package edu.bluejack24_2.ecoai.utils

import edu.bluejack24_2.ecoai.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

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

            db.collection("notifications").add(notification)

            val timestamp = Timestamp.now()
            val notificationMap = mapOf(
                "fromUserId" to fromUserId,
                "toUserId" to toUserId,
                "postId" to (postId ?: ""),
                "type" to type,
                "createdAt" to mapOf(
                    "seconds" to timestamp.seconds,
                    "nanoseconds" to timestamp.nanoseconds
                )
            )
            FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(toUserId)
                .push()
                .setValue(notificationMap)
        }
}