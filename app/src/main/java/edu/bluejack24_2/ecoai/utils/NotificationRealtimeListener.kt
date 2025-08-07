package edu.bluejack24_2.ecoai.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import edu.bluejack24_2.ecoai.MainActivity
import edu.bluejack24_2.ecoai.R
import edu.bluejack24_2.ecoai.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class NotificationRealtimeListener {

    private var attachTime: Long = 0L

    fun start(context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("notifications")
            .child(currentUserId)

        attachTime = System.currentTimeMillis()

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val map = snapshot.value as? Map<String, Any> ?: return

                val createdAtMap = map["createdAt"] as? Map<String, Any>
                val timestamp = if (createdAtMap != null) {
                    val seconds = (createdAtMap["seconds"] as? Number)?.toLong() ?: 0L
                    val nanos = (createdAtMap["nanoseconds"] as? Number)?.toInt() ?: 0
                    com.google.firebase.Timestamp(seconds, nanos)
                } else {
                    com.google.firebase.Timestamp.now()
                }

                if (timestamp.toDate().time < attachTime) return

                val notification = Notification(
                    fromUserId = map["fromUserId"] as? String ?: "",
                    toUserId = map["toUserId"] as? String ?: "",
                    postId = map["postId"] as? String ?: "",
                    type = map["type"] as? String ?: "",
                    createdAt = timestamp
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(notification.fromUserId)
                    .get()
                    .addOnSuccessListener { document ->
                        val username = document.getString("username") ?: "Someone"
                        showSystemNotification(context, notification, username)
                    }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showSystemNotification(
        context: Context,
        notification: Notification,
        username: String
    ) {
        val actionMessage = when (notification.type) {
            "like" -> LanguageManager.getString("notif_like")
            "comment" -> LanguageManager.getString("notif_comment")
            "follow" -> LanguageManager.getString("notif_follow")
            else -> ""
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("postId", notification.postId)
            putExtra("navigateTo", if (notification.type == "follow") "user_profile" else "post_detail")
            putExtra("userId", notification.fromUserId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "NOTIF_CHANNEL_ID")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("New Notification")
            .setContentText("$username $actionMessage")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

}
