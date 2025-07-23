package com.bluejack242.ecoai.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bluejack242.ecoai.model.Notification
import com.bluejack242.ecoai.model.User
import com.bluejack242.ecoai.utils.LanguageManager
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

@Composable
fun NotificationCard(
    notification: Notification,
    fromUser: User,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    val actionMessage = when (notification.type) {
        "like" -> LanguageManager.getString("notif_like")
        "comment" -> LanguageManager.getString("notif_comment")
        "follow" -> LanguageManager.getString("notif_follow")
        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (notification.type != "follow") {
                    onPostClick(notification.postId)
                } else {
                    onUserClick(fromUser.uid)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = fromUser.profilePicUrl,
                contentDescription = "User Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onUserClick(fromUser.uid) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "${fromUser.displayName} $actionMessage",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = timeAgo(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

fun timeAgo(timestamp: Timestamp): String {
    val now = System.currentTimeMillis()
    val time = timestamp.toDate().time
    val diff = now - time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> LanguageManager.getString("time_just_now")
        minutes < 60 -> LanguageManager.getString("time_minutes_ago").format(minutes)
        hours < 24 -> LanguageManager.getString("time_hours_ago").format(hours)
        days < 7 -> LanguageManager.getString("time_days_ago").format(days)
        days < 30 -> LanguageManager.getString("time_weeks_ago").format(days / 7)
        days < 365 -> LanguageManager.getString("time_months_ago").format(days / 30)
        else -> LanguageManager.getString("time_years_ago").format(days / 365)
    }
}