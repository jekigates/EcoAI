package com.bluejack242.ecoai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person

@Composable
fun MediaCard(
    imageUrl: String,
    title: String,
    fullName: String,
    profilePictureUrl: String?,
    likes: Int,
    liked: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Media image",
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!profilePictureUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(fullName, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Likes",
                    modifier = Modifier.size(14.dp),
                    tint = if (liked) Color.Red else Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = likes.toString(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
