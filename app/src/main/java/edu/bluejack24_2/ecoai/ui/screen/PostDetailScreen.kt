package edu.bluejack24_2.ecoai.ui.screen

import android.net.Uri
import edu.bluejack24_2.ecoai.ui.component.CustomDialog
import edu.bluejack24_2.ecoai.ui.component.DialogType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.BookmarkPlus
import com.composables.icons.lucide.Download
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import edu.bluejack24_2.ecoai.viewmodel.PostDetailViewModel
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import edu.bluejack24_2.ecoai.utils.sendNotificationWithType
import edu.bluejack24_2.ecoai.utils.LanguageManager
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.ArrowUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavHostController,
    viewModel: PostDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val mediaList = viewModel.getMediaList()

    var commentInput by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoadingComments by remember { mutableStateOf(true) }
    val userId = viewModel.userId
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    LaunchedEffect(postId) {
        isLoadingComments = true
        db.collection("posts").document(postId).collection("comments")
            .addSnapshotListener { snapshot, _ ->
                comments =
                    snapshot?.documents?.mapNotNull { it.data?.plus("id" to it.id) } ?: emptyList()
                isLoadingComments = false
            }
    }

    // Add comment
    fun addComment() {
        val trimmed = commentInput.trim()
        val creatorUid = viewModel.creator?.get("uid") as? String ?: ""
        if (trimmed.isNotEmpty() && userId != null) {
            db.collection("posts").document(postId).collection("comments")
                .add(
                    mapOf(
                        "userId" to userId,
                        "text" to trimmed,
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "likedBy" to emptyList<String>()
                    )
                )
            commentInput = ""
            sendNotificationWithType(
                fromUserId = userId,
                toUserId = creatorUid,
                type = "comment",
                postId = postId
            )

        }
    }

    // Like/unlike comment
    fun toggleCommentLike(commentId: String, liked: Boolean) {
        if (userId == null) return
        val ref = db.collection("posts").document(postId).collection("comments").document(commentId)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val likedBy = (snap.get("likedBy") as? List<*>)?.map { it.toString() }?.toMutableList()
                ?: mutableListOf()
            if (liked) likedBy.remove(userId) else likedBy.add(userId)
            tx.update(ref, "likedBy", likedBy)

        }
    }

    // Bottom bar state
    var showCommentSheet by remember { mutableStateOf(false) }
    val emojiList = listOf("🍋", "🥰", "🤣", "👍", "❤️", "😂", "🥺", "✨")

    fun deleteComment(commentId: String) {
        if (userId == null) return
        viewModel.deleteComment(postId, commentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = LanguageManager.getString("back")
                )
            }
            if (viewModel.creator != null) {
                val profilePictureUrl = viewModel.creator?.get("profilePictureUrl") as? String
                val creatorUid = viewModel.creator?.get("uid") as? String ?: ""
                val isSelf = viewModel.userId == creatorUid
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (creatorUid.isNotBlank()) navController.navigate("user_profile/$creatorUid")
                    }
                ) {
                    if (!profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profilePictureUrl,
                            contentDescription = "${LanguageManager.getString("profile_picture")} ${viewModel.creator?.get("fullName") ?: ""}",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "${LanguageManager.getString("profile_picture")} ${viewModel.creator?.get("fullName") ?: ""}",
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        (viewModel.creator?.get("fullName") as? String)?.take(30) ?: "Unknown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isSelf) {
                    Button(
                        onClick = { viewModel.toggleFollow() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isFollowing) Color.Gray else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            if (viewModel.isFollowing) LanguageManager.getString("unfollow") else LanguageManager.getString("follow"),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    IconButton(onClick = { viewModel.showBottomSheet = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = LanguageManager.getString("more")
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Post photo(s)
        if (mediaList.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { mediaList.size })
            viewModel.currentPagerIndex = pagerState.currentPage
            LaunchedEffect(pagerState.currentPage) {
                viewModel.showPagerIndicatorWithDelay()
            }
            Column {
                Box(Modifier) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.LightGray)
                    ) { index ->
                        val media = mediaList[index]
                        Box(Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = media["url"],
                                contentDescription = LanguageManager.getString("post_image"),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (mediaList.size > 1 && viewModel.showPagerIndicator) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${mediaList.size}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (mediaList.size > 1) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(mediaList.size) { i ->
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
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Created date
        val createdDateString = viewModel.getCreatedDateString()
        if (createdDateString.isNotBlank()) {
            Text(
                text = createdDateString,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        // Headline
        val headline = viewModel.post?.get("headline") as? String ?: ""
        if (headline.isNotBlank()) {
            Text(
                text = headline,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        val captionText = viewModel.post?.get("caption") as? String ?: ""
        if (captionText.isNotBlank()) {
            val hashtagRegex = Regex("#[A-Za-z0-9_]+")
            val annotatedCaption = buildAnnotatedString {
                var lastIndex = 0
                for (match in hashtagRegex.findAll(captionText)) {
                    val start = match.range.first
                    val end = match.range.last + 1

                    if (start > lastIndex) {
                        append(captionText.substring(lastIndex, start))
                    }

                    pushStringAnnotation(
                        tag = "TAG",
                        annotation = captionText.substring(start + 1, end)
                    )
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(captionText.substring(start, end))
                    }
                    pop()

                    lastIndex = end
                }
                if (lastIndex < captionText.length) {
                    append(captionText.substring(lastIndex))
                }
            }

            ClickableText(
                text = annotatedCaption,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                onClick = { offset ->
                    annotatedCaption.getStringAnnotations("TAG", offset, offset)
                        .firstOrNull()?.let { annotation ->
                            navController.navigate("search/${Uri.encode(annotation.item)}")
                        }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Comments section
        Text(
            "${comments.size} ${LanguageManager.getString("comments")}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (isLoadingComments) {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (comments.isEmpty()) {
            Text(
                LanguageManager.getString("no_comments"),
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Column(Modifier.padding(horizontal = 16.dp)) {
                comments.forEach { comment ->
                    val commentId = comment["id"] as String
                    val text = comment["text"] as? String ?: ""
                    val likedBy = comment["likedBy"] as? List<*> ?: emptyList<Any>()
                    val liked = userId != null && likedBy.contains(userId)
                    val likeCount = likedBy.size
                    val userIdOfComment = comment["userId"] as? String ?: ""
                    var commenterName by remember(commentId) { mutableStateOf("") }
                    var commenterProfilePic by remember(commentId) { mutableStateOf<String?>(null) }
                    val createdAt = comment["createdAt"] as? com.google.firebase.Timestamp
                    val commentCreatedDateString = remember(createdAt) {
                        createdAt?.let {
                            val date = java.util.Date(it.seconds * 1000)
                            java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                                .format(date)
                        } ?: ""
                    }
                    val isSelfComment = userId == userIdOfComment
                    var showCommentBottomSheet by remember { mutableStateOf(false) }

                    LaunchedEffect(userIdOfComment) {
                        if (userIdOfComment.isNotBlank()) {
                            db.collection("users").document(userIdOfComment).get()
                                .addOnSuccessListener { doc ->
                                    commenterName = doc.getString("fullName") ?: ""
                                    commenterProfilePic = doc.getString("profilePictureUrl")
                                }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .clickable {
                                    if (userIdOfComment.isNotBlank()) navController.navigate("user_profile/$userIdOfComment")
                                }
                        ) {
                            if (!commenterProfilePic.isNullOrBlank()) {
                                AsyncImage(
                                    model = commenterProfilePic,
                                    contentDescription = "${LanguageManager.getString("profile_picture")} ${
                                        viewModel.creator?.get(
                                            "fullName"
                                        ) ?: ""
                                    }",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "${LanguageManager.getString("profile_picture")} ${
                                        viewModel.creator?.get(
                                            "fullName"
                                        ) ?: ""
                                    }",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        // Name, comment, date (vertical)
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                commenterName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                commentCreatedDateString,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        // Like button and count
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = LanguageManager.getString("like_comment"),
                                        tint = if (liked) Color.Red else MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.7f
                                        ),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { toggleCommentLike(commentId, liked) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                likeCount.toString(),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        if (isSelfComment) {
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { showCommentBottomSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = LanguageManager.getString("more"),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    if (showCommentBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showCommentBottomSheet = false },
                            sheetState = rememberModalBottomSheetState()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                ListItem(
                                    headlineContent = { Text(LanguageManager.getString("delete")) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        showCommentBottomSheet = false
                                        deleteComment(commentId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        // Bottom bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable { showCommentSheet = true }
                ) {
                    BasicTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        singleLine = true,
                        enabled = false,
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxWidth()) {
                                if (commentInput.isEmpty()) Text(
                                    LanguageManager.getString("add_comment_placeholder"),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { viewModel.toggleLike(postId) }) {
                    Icon(
                        imageVector = if (viewModel.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = LanguageManager.getString("like"),
                        tint = if (viewModel.isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = viewModel.likeCount.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Lucide.MessageCircle,
                    contentDescription = LanguageManager.getString("comments"),
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = comments.size.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { viewModel.toggleSave(postId) }) {
                    Icon(
                        imageVector = if (viewModel.isSaved) Lucide.Bookmark else Lucide.BookmarkPlus,
                        contentDescription = LanguageManager.getString("save"),
                        tint = if (viewModel.isSaved) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = viewModel.saveCount.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
    // Comment modal bottom sheet
    if (showCommentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .navigationBarsPadding()
            ) {
                // Emoji row
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    emojiList.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .padding(2.dp)
                                .clickable { commentInput += emoji }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val buttonVisible = commentInput.isNotBlank()
                    val buttonWidth = 36.dp
                    val gapWidth = if (buttonVisible) 16.dp else 0.dp
                    BasicTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        maxLines = 4,
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxSize()) {
                                if (commentInput.isEmpty()) Text(
                                    LanguageManager.getString("add_comment_placeholder"),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                innerTextField()
                            }
                        }
                    )
                    if (buttonVisible) {
                        Spacer(Modifier.width(gapWidth))
                        IconButton(
                            onClick = {
                                addComment()
                                showCommentSheet = false
                            },
                            modifier = Modifier
                                .size(buttonWidth)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Lucide.ArrowUp,
                                contentDescription = LanguageManager.getString("send"),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                // ...existing code...
            }
        }
    }
    if (viewModel.showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(Modifier.fillMaxWidth()) {
                // Download photo
                ListItem(
                    headlineContent = { Text(LanguageManager.getString("download_photo")) },
                    leadingContent = { Icon(Lucide.Download, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.showBottomSheet = false
                        val url = mediaList.getOrNull(viewModel.currentPagerIndex)?.get("url")
                        if (url != null) {
                            viewModel.downloadPhoto(context, url)
                        }
                    }
                )
                // Edit
                ListItem(
                    headlineContent = { Text(LanguageManager.getString("edit")) },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.showBottomSheet = false
                    }
                )
                // Delete
                ListItem(
                    headlineContent = { Text(LanguageManager.getString("delete")) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.showBottomSheet = false
                        viewModel.showDeleteDialog = true
                    }
                )
            }
        }
    }
    // Delete confirmation dialog
    if (viewModel.showDeleteDialog) {
        CustomDialog(
            title = LanguageManager.getString("delete_confirmation"),
            message = LanguageManager.getString("delete_post_confirmation_message"),
            confirmText = LanguageManager.getString("delete"),
            onConfirm = {
                viewModel.showDeleteDialog = false
                viewModel.deletePost(postId) {
                    navController.popBackStack()
                }
            },
            onDismiss = { viewModel.showDeleteDialog = false },
            dialogType = DialogType.Confirm,
            showDismiss = true,
            dismissText = LanguageManager.getString("cancel")
        )
    }
} 