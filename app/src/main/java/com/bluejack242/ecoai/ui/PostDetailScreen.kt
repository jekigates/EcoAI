package com.bluejack242.ecoai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
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

@Composable
fun PostDetailScreen(postId: String, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    var isLoading by remember { mutableStateOf(true) }
    var post by remember { mutableStateOf<Map<String, Any>?>(null) }
    var creator by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var saveCount by remember { mutableIntStateOf(0) }

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
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Post photo(s)
        val mediaList = (post?.get("media") as? List<*>)?.mapNotNull {
            it as? Map<*, *>
        }?.map { map ->
            map.mapKeys { it.key.toString() }.mapValues { it.value?.toString() ?: "" }
        } ?: emptyList()
        if (mediaList.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { mediaList.size })
            Column {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.LightGray)
                ) { index ->
                    val media = mediaList[index]
                    AsyncImage(
                        model = media["url"],
                        contentDescription = "Post Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Pager indicator
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
        Spacer(modifier = Modifier.height(16.dp))
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
} 