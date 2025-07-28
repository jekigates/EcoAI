package com.bluejack242.ecoai.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.text.ClickableText


@Composable
fun FollowingPostCard(
    profilePictureUrl: String? = null,
    username: String,
    title: String,
    imageUrl: String? = null,
    caption: String,
    likes: Int = 0,
    comments: Int = 0,
    saves: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(likes) }

    var isSaved by remember { mutableStateOf(false) }
    var saveCount by remember { mutableStateOf(saves) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Profile Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (profilePictureUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(profilePictureUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(username, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Post Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("No Image", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Caption with tag hyperlink
        val annotatedCaption = buildAnnotatedString {
            val words = caption.split(" ")
            words.forEachIndexed { index, word ->
                if (word.startsWith("#")) {
                    val tag = word.removePrefix("#")
                    pushStringAnnotation(tag = "TAG", annotation = tag)
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(word)
                    }
                    pop()
                } else {
                    append(word)
                }
                if (index != words.lastIndex) append(" ")
            }
        }

        ClickableText(
            text = annotatedCaption,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            onClick = { offset ->
                annotatedCaption.getStringAnnotations(tag = "TAG", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val tag = annotation.item
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$tag"))
                        context.startActivity(intent)
                    }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Interaction Icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    isLiked = !isLiked
                    likeCount += if (isLiked) 1 else -1
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(likeCount.toString(), style = MaterialTheme.typography.labelSmall)
            }

            // Comment
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(comments.toString(), style = MaterialTheme.typography.labelSmall)
            }

            // Save
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    isSaved = !isSaved
                    saveCount += if (isSaved) 1 else -1
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Save",
                    tint = if (isSaved) Color.Yellow else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(saveCount.toString(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

