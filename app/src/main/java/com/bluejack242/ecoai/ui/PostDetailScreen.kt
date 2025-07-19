package com.bluejack242.ecoai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.google.firebase.firestore.FirebaseFirestore
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.BookmarkPlus
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.statusBarsPadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import com.composables.icons.lucide.Download
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(postId: String, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var post by remember { mutableStateOf<Map<String, Any>?>(null) }
    var creator by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var saveCount by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    var currentPagerIndex by remember { mutableIntStateOf(0) }
    var showPagerIndicator by remember { mutableStateOf(false) }
    var pagerIndicatorJob by remember { mutableStateOf<Job?>(null) }

    // Move mediaList declaration here so it's accessible everywhere in the composable
    val mediaList = (post?.get("media") as? List<*>)?.mapNotNull {
        it as? Map<*, *>
    }?.map { map ->
        map.mapKeys { it.key.toString() }.mapValues { it.value?.toString() ?: "" }
    } ?: emptyList()

    // Fetch post and creator info, and like/save state
    LaunchedEffect(postId, userId) {
        db.collection("posts").document(postId).get().addOnSuccessListener { doc ->
            post = doc.data
            val creatorId = doc.getString("userId") ?: ""
            likeCount = (doc.get("likedBy") as? List<*>)?.size ?: 0
            saveCount = (doc.get("savedBy") as? List<*>)?.size ?: 0
            isLiked = userId != null && (doc.get("likedBy") as? List<*>)?.contains(userId) == true
            isSaved = userId != null && (doc.get("savedBy") as? List<*>)?.contains(userId) == true
            if (creatorId.isNotBlank()) {
                db.collection("users").document(creatorId).get().addOnSuccessListener { userDoc ->
                    creator = userDoc.data
                    // Check if current user is following the creator
                    if (userId != null) {
                        db.collection("users").document(userId).get().addOnSuccessListener { currentUserDoc ->
                            val following = (currentUserDoc.get("following") as? List<*>)?.map { it.toString() } ?: emptyList()
                            isFollowing = following.contains(creatorId)
                            isLoading = false
                        }
                    } else {
                        isLoading = false
                    }
                }
            } else {
                isLoading = false
            }
        }
    }

    fun toggleLike() {
        if (userId == null) return
        val postRef = db.collection("posts").document(postId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likedBy = (snapshot.get("likedBy") as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (likedBy.contains(userId)) {
                likedBy.remove(userId)
            } else {
                likedBy.add(userId)
            }
            transaction.update(postRef, "likedBy", likedBy)
            transaction.update(postRef, "likes", likedBy.size)
        }.addOnSuccessListener {
            isLiked = !isLiked
            likeCount += if (isLiked) 1 else -1
        }
    }
    fun toggleSave() {
        if (userId == null) return
        val postRef = db.collection("posts").document(postId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val savedBy = (snapshot.get("savedBy") as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (savedBy.contains(userId)) {
                savedBy.remove(userId)
            } else {
                savedBy.add(userId)
            }
            transaction.update(postRef, "savedBy", savedBy)
        }.addOnSuccessListener {
            isSaved = !isSaved
            saveCount += if (isSaved) 1 else -1
        }
    }

    fun toggleFollow() {
        if (userId == null || creator == null) return
        val creatorId = creator?.get("uid") as? String ?: return
        val currentUserRef = db.collection("users").document(userId)
        val creatorRef = db.collection("users").document(creatorId)
        db.runBatch { batch ->
            if (isFollowing) {
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(creatorId))
                batch.update(creatorRef, "followers", FieldValue.arrayRemove(userId))
            } else {
                batch.update(currentUserRef, "following", FieldValue.arrayUnion(creatorId))
                batch.update(creatorRef, "followers", FieldValue.arrayUnion(userId))
            }
        }.addOnSuccessListener {
            isFollowing = !isFollowing
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

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
            if (creator != null) {
                val profilePictureUrl = creator?.get("profilePictureUrl") as? String
                val creatorUid = creator?.get("uid") as? String ?: ""
                val isSelf = userId == creatorUid
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
                Text((creator?.get("fullName") as? String)?.take(30) ?: "Unknown", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                if (!isSelf) {
                    Button(
                        onClick = { toggleFollow() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) Color.Gray else Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (isFollowing) "Unfollow" else "Follow", color = Color.White, fontSize = 14.sp)
                    }
                } else {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Post photo(s)
        if (mediaList.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { mediaList.size })
            currentPagerIndex = pagerState.currentPage
            // Show/hide pagination indicator on page change
            LaunchedEffect(pagerState.currentPage) {
                showPagerIndicator = true
                pagerIndicatorJob?.cancel()
                pagerIndicatorJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(1500)
                    showPagerIndicator = false
                }
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
                    if (mediaList.size > 1 && showPagerIndicator) {
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
        val createdAt = post?.get("createdAt")
        val createdDateString = remember(createdAt) {
            (createdAt as? com.google.firebase.Timestamp)?.let {
                val date = Date(it.seconds * 1000)
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
            } ?: ""
        }
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
        Text(
            text = post?.get("headline") as? String ?: "",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Caption
        Text(
            text = post?.get("caption") as? String ?: "",
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Like/Save buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { toggleLike() }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
            Text(text = likeCount.toString(), fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { toggleSave() }) {
                Icon(
                    imageVector = if (isSaved) Lucide.Bookmark else Lucide.BookmarkPlus,
                    contentDescription = "Save",
                    tint = if (isSaved) Color(0xFF4CAF50) else Color.Gray
                )
            }
            Text(text = saveCount.toString(), fontSize = 14.sp, color = Color.Gray)
        }
    }
    // BottomSheet for self post
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            Column(Modifier.fillMaxWidth()) {
                // Download photo
                ListItem(
                    headlineContent = { Text("Download photo") },
                    leadingContent = { Icon(Lucide.Download, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showBottomSheet = false
                        val url = mediaList.getOrNull(currentPagerIndex)?.get("url")
                        if (url != null) {
                            val request = DownloadManager.Request(url.toUri())
                                .setTitle("ecoai_image.jpg")
                                .setDescription("Downloading image...")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ecoai_image.jpg")
                                .setAllowedOverMetered(true)
                                .setAllowedOverRoaming(true)
                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            dm.enqueue(request)
                        }
                    }
                )
                // Edit
                ListItem(
                    headlineContent = { Text("Edit") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showBottomSheet = false
                        // TODO: Edit action
                    }
                )
                // Delete
                ListItem(
                    headlineContent = { Text("Delete") },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showBottomSheet = false
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    // Delete post logic
                    db.collection("posts").document(postId).delete().addOnSuccessListener {
                        navController.popBackStack()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
} 