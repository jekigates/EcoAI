package com.bluejack242.ecoai.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.filled.FavoriteBorder
import com.bluejack242.ecoai.utils.LanguageManager
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.BookmarkPlus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle

@Composable
fun FollowingPostCard(
    postId: String,
    profilePictureUrl: String? = null,
    username: String,
    title: String,
    imageUrl: String? = null,
    caption: String,
    likes: Int = 0,
    comments: List<Map<String, Any>> = emptyList(),
    saves: Int = 0,
    isLiked: Boolean = false,
    isSaved: Boolean = false,
    onLikeClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentLikes by remember { mutableIntStateOf(likes) }
    var currentSaves by remember { mutableIntStateOf(saves) }
    var liked by remember { mutableStateOf(isLiked) }
    var saved by remember { mutableStateOf(isSaved) }

    LaunchedEffect(likes, saves, isLiked, isSaved) {
        currentLikes = likes
        currentSaves = saves
        liked = isLiked
        saved = isSaved
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
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
            Text("@$username", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

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
                        navController.navigate("search?query=$tag")
                    }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    liked = !liked
                    currentLikes += if (liked) 1 else -1
                    onLikeClick(postId)
                }
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (liked) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(currentLikes.toString(), style = MaterialTheme.typography.labelSmall)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onCommentClick(postId)
                }
            ) {
                Icon(
                    imageVector = Lucide.MessageCircle,
                    contentDescription = "Comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(comments.size.toString(), style = MaterialTheme.typography.labelSmall)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    saved = !saved
                    currentSaves += if (saved) 1 else -1
                    onSaveClick(postId)
                }
            ) {
                Icon(
                    imageVector = if (saved) Lucide.Bookmark else Lucide.BookmarkPlus,
                    contentDescription = "Save",
                    tint = if (saved) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(currentSaves.toString(), style = MaterialTheme.typography.labelSmall)
            }
        }

        if (comments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LanguageManager.getString("top_comments"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            comments.take(3).forEach { comment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val commentProfileUrl = comment["profilePictureUrl"] as? String
                    if (commentProfileUrl != null) {
                        AsyncImage(
                            model = commentProfileUrl,
                            contentDescription = "Commenter Profile",
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "@${comment["username"] ?: "Unknown"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comment["text"] as? String ?: "",
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${comment["likes"] ?: 0}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
