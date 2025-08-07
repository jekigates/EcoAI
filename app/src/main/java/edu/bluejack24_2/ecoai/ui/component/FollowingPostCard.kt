package edu.bluejack24_2.ecoai.ui.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.viewmodel.HomeViewModel
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.BookmarkPlus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun FollowingPostCard(
    postId: String,
    profilePictureUrl: String? = null,
    userId: String,
    fullName: String,
    title: String,
    createdAt: Any? = null,
    imageUrl: String? = null,
    mediaList: List<Map<String, Any>> = emptyList(),
    caption: String,
    likes: Int = 0,
    saves: Int = 0,
    isLiked: Boolean = false,
    isSaved: Boolean = false,
    onLikeClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    var currentLikes by remember { mutableIntStateOf(likes) }
    var currentSaves by remember { mutableIntStateOf(saves) }
    var liked by remember { mutableStateOf(isLiked) }
    var saved by remember { mutableStateOf(isSaved) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isSelf = currentUser?.uid == userId
    val commentList by remember { derivedStateOf { viewModel.commentsMap[postId] ?: emptyList() } }

    LaunchedEffect(likes, saves, isLiked, isSaved) {
        currentLikes = likes
        currentSaves = saves
        liked = isLiked
        saved = isSaved
    }

    LaunchedEffect(postId) {
        if (!viewModel.commentsMap.containsKey(postId)) {
            viewModel.loadComments(postId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate("user_profile/$userId") }
            ) {
                if (profilePictureUrl != null) {
                    EcoAsyncImage(
                        imageUrl = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        shape = CircleShape
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
                Column {
                Text(fullName, fontWeight = FontWeight.Bold)
                    val dateString = remember(createdAt) {
                        createdAt?.let {
                            try {
                                val timestamp = it as? com.google.firebase.Timestamp
                                val date = timestamp?.let { t -> java.util.Date(t.seconds * 1000) }
                                    ?: (it as? java.util.Date)
                                date?.let { d ->
                                    java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(d)
                                } ?: ""
                            } catch (e: Exception) { "" }
                        } ?: ""
                    }
                    if (dateString.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(dateString, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (isSelf) {
                IconButton(onClick = { showBottomSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = LanguageManager.getString("more"))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        val pagerMediaList = if (mediaList.isNotEmpty()) mediaList else if (!imageUrl.isNullOrEmpty()) listOf(mapOf("url" to imageUrl)) else emptyList()
        val pagerState = if (pagerMediaList.isNotEmpty()) androidx.compose.foundation.pager.rememberPagerState(pageCount = { pagerMediaList.size }) else null
        var showPagerIndicator by remember { mutableStateOf(false) }
        if (pagerMediaList.isNotEmpty() && pagerState != null) {
            LaunchedEffect(pagerState.currentPage) {
                showPagerIndicator = true
                kotlinx.coroutines.delay(1200)
                showPagerIndicator = false
            }
            Box {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.LightGray)
                ) { index ->
                    val media = pagerMediaList[index]
                    val url = media["url"] as? String
                    if (!url.isNullOrEmpty()) {
                        EcoAsyncImage(
                            imageUrl = url,
                            contentDescription = "Post Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("No Image", color = Color.DarkGray)
                    }
                }
                if (pagerMediaList.size > 1 && showPagerIndicator) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${pagerMediaList.size}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (title.isNotBlank() || caption.isNotBlank()) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            val annotatedCaption = buildAnnotatedString {
                val words = caption.split(" ")
                words.forEachIndexed { index, word ->
                    if (word.startsWith("#")) {
                        val tag = word.removePrefix("#")
                        pushStringAnnotation(tag = "TAG", annotation = tag)
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.SemiBold
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

            if (caption.isNotBlank()) {
                ClickableText(
                    text = annotatedCaption,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    onClick = { offset ->
                        annotatedCaption.getStringAnnotations(tag = "TAG", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val tag = annotation.item
                                navController.navigate("search/${Uri.encode(tag)}")
                            }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        liked = !liked
                        currentLikes += if (liked) 1 else -1
                        onLikeClick(postId)
                    }
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (liked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                if (currentLikes > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(currentLikes.toString(), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onCommentClick(postId) }
                    .padding(start = 20.dp)
            ) {
                Icon(
                    imageVector = Lucide.MessageCircle,
                    contentDescription = "Comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                if (commentList.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(commentList.size.toString(), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (pagerMediaList.size > 1 && pagerState != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(pagerMediaList.size) { i ->
                        Box(
                            Modifier
                                .size(if (pagerState.currentPage == i) 10.dp else 8.dp)
                                .padding(2.dp)
                                .background(
                                    if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        saved = !saved
                        currentSaves += if (saved) 1 else -1
                        onSaveClick(postId)
                    }
            ) {
                Icon(
                    imageVector = if (saved) Lucide.Bookmark else Lucide.BookmarkPlus,
                    contentDescription = "Save",
                    tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                if (currentSaves > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(currentSaves.toString(), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        }

        if (commentList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LanguageManager.getString("top_comments"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            commentList.take(3).forEach { comment ->
                val commentId = comment["id"] as? String
                val likedBy = comment["likedBy"] as? List<*> ?: emptyList<Any>()
                val isCommentLiked = currentUserId != null && likedBy.contains(currentUserId)
                val likeCount = likedBy.size
                var localLiked by remember(commentId) { mutableStateOf(isCommentLiked) }
                var localLikeCount by remember(commentId) { mutableIntStateOf(likeCount) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val commentProfileUrl = comment["profilePictureUrl"] as? String
                    if (commentProfileUrl != null) {
                        EcoAsyncImage(
                            imageUrl = commentProfileUrl,
                            contentDescription = "Commenter Profile",
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape),
                            shape = CircleShape
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(enabled = commentId != null && currentUserId != null) {
                                if (commentId != null && currentUserId != null) {
                                    val ref = db.collection("posts").document(postId).collection("comments").document(commentId)
                                    db.runTransaction { tx ->
                                        val snap = tx.get(ref)
                                        val likedByList = (snap.get("likedBy") as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
                                        if (localLiked) likedByList.remove(currentUserId) else likedByList.add(currentUserId)
                                        tx.update(ref, "likedBy", likedByList)
                                    }
                                    localLiked = !localLiked
                                    localLikeCount += if (localLiked) 1 else -1
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (localLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like Comment",
                            tint = if (localLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = localLikeCount.toString(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }

        if (showBottomSheet && isSelf) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(LanguageManager.getString("delete")) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        if (showDeleteDialog) {
            CustomDialog(
                title = LanguageManager.getString("delete_confirmation"),
                message = LanguageManager.getString("delete_post_confirmation_message"),
                confirmText = LanguageManager.getString("delete"),
                onConfirm = {
                    showDeleteDialog = false
                    onDelete(postId)
                },
                onDismiss = { showDeleteDialog = false },
                dialogType = DialogType.Confirm,
                showDismiss = true,
                dismissText = LanguageManager.getString("cancel")
            )
        }
    }
}
