package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import com.bluejack242.ecoai.viewmodel.PostDetailViewModel
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.alpha
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.ArrowUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(postId: String, navController: NavHostController, viewModel: PostDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    // Load post on first composition
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

    // Comments state
    var commentInput by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoadingComments by remember { mutableStateOf(true) }
    val userId = viewModel.userId
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    // Fetch comments
    LaunchedEffect(postId) {
        isLoadingComments = true
        db.collection("posts").document(postId).collection("comments")
            .addSnapshotListener { snapshot, _ ->
                comments = snapshot?.documents?.mapNotNull { it.data?.plus("id" to it.id) } ?: emptyList()
                isLoadingComments = false
            }
    }

    // Add comment
    fun addComment() {
        val trimmed = commentInput.trim()
        if (trimmed.isNotEmpty() && userId != null) {
            db.collection("posts").document(postId).collection("comments")
                .add(mapOf(
                    "userId" to userId,
                    "text" to trimmed,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "likedBy" to emptyList<String>()
                ))
            commentInput = ""
        }
    }

    // Like/unlike comment
    fun toggleCommentLike(commentId: String, liked: Boolean) {
        if (userId == null) return
        val ref = db.collection("posts").document(postId).collection("comments").document(commentId)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val likedBy = (snap.get("likedBy") as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (liked) likedBy.remove(userId) else likedBy.add(userId)
            tx.update(ref, "likedBy", likedBy)
        }
    }

    // Bottom bar state
    var showCommentSheet by remember { mutableStateOf(false) }
    val emojiList = listOf("🍋", "🥰", "🤣", "👍", "❤️", "😂", "🥺", "✨")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text((viewModel.creator?.get("fullName") as? String)?.take(30) ?: "Unknown", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isSelf) {
                    Button(
                        onClick = { viewModel.toggleFollow() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isFollowing) Color.Gray else Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (viewModel.isFollowing) "Unfollow" else "Follow", color = Color.White, fontSize = 14.sp)
                    }
                } else {
                    IconButton(onClick = { viewModel.showBottomSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Post photo(s)
        if (mediaList.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { mediaList.size })
            viewModel.currentPagerIndex = pagerState.currentPage
            // Show/hide pagination indicator on page change
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
                                contentDescription = "Post Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    // Pagination indicator at top right, only if more than 1 photo and while sliding
                    if (mediaList.size > 1 && viewModel.showPagerIndicator) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${mediaList.size}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color(0x80000000), shape = CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    // Dots indicator only if more than 1 photo
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
                                            if (pagerState.currentPage == i) Color(0xFF4CAF50) else Color.LightGray,
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
                if (start > lastIndex) append(captionText.substring(lastIndex, start))
                withStyle(SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)) {
                    append(captionText.substring(start, end))
                }
                lastIndex = end
            }
            if (lastIndex < captionText.length) append(captionText.substring(lastIndex))
        }
        Text(
            text = annotatedCaption,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Comments section
        Text("${comments.size} comments", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        if (isLoadingComments) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (comments.isEmpty()) {
            Text("No comments yet", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
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
                    val createdDateString = remember(createdAt) {
                        createdAt?.let {
                            val date = java.util.Date(it.seconds * 1000)
                            java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(date)
                        } ?: ""
                    }
                    // Fetch commenter info
                    LaunchedEffect(userIdOfComment) {
                        if (userIdOfComment.isNotBlank()) {
                            db.collection("users").document(userIdOfComment).get().addOnSuccessListener { doc ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (userIdOfComment.isNotBlank()) navController.navigate("user_profile/$userIdOfComment")
                            }
                        ) {
                            if (!commenterProfilePic.isNullOrBlank()) {
                                AsyncImage(
                                    model = commenterProfilePic,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E0E0))
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(commenterName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Gray)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(text, fontSize = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(createdDateString, fontSize = 13.sp, color = Color.Gray)
                                Spacer(Modifier.width(12.dp))
                                Text("Reply", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.alpha(0f)) // hidden, for layout
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Like comment",
                                    tint = if (liked) Color.Red else Color.Gray,
            modifier = Modifier
                                        .size(20.dp)
                                        .clickable { toggleCommentLike(commentId, liked) }
                                )
                            }
                            Text(likeCount.toString(), fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        // Bottom bar
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
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
                        .background(Color(0xFFF0F0F0), shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable { showCommentSheet = true }
                ) {
                    BasicTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        singleLine = true,
                        enabled = false,
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxWidth()) {
                                if (commentInput.isEmpty()) Text("Add comment...", color = Color.Gray)
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.toggleLike(postId) }) {
                Icon(
                    imageVector = if (viewModel.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                        tint = if (viewModel.isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(text = viewModel.likeCount.toString(), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Lucide.MessageCircle,
                    contentDescription = "Comments",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = comments.size.toString(), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { viewModel.toggleSave(postId) }) {
                Icon(
                    imageVector = if (viewModel.isSaved) Lucide.Bookmark else Lucide.BookmarkPlus,
                    contentDescription = "Save",
                        tint = if (viewModel.isSaved) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(text = viewModel.saveCount.toString(), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
            }
        }
    }
    // Comment modal bottom sheet
    if (showCommentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Emoji row
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    emojiList.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { commentInput += emoji }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Large text area
                BasicTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color(0xFFF0F0F0), shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    maxLines = 4,
                    decorationBox = { innerTextField ->
                        Box(Modifier.fillMaxSize()) {
                            if (commentInput.isEmpty()) Text("Add comment...", color = Color.Gray)
                            innerTextField()
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                // Arrow up icon button
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = {
                            addComment()
                            showCommentSheet = false
                        },
                        enabled = commentInput.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Yellow, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Lucide.ArrowUp,
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
    // BottomSheet for self post
    if (viewModel.showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(Modifier.fillMaxWidth()) {
                // Download photo
                ListItem(
                    headlineContent = { Text("Download photo") },
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
                    headlineContent = { Text("Edit") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.showBottomSheet = false
                        // TODO: Edit action
                    }
                )
                // Delete
                ListItem(
                    headlineContent = { Text("Delete") },
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
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog = false },
            title = { Text("Delete?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.showDeleteDialog = false
                    viewModel.deletePost(postId) {
                        navController.popBackStack()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
} 