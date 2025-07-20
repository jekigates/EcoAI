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
        Text(
            text = viewModel.post?.get("headline") as? String ?: "",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Caption with styled hashtags
        val captionText = viewModel.post?.get("caption") as? String ?: ""
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
        Spacer(modifier = Modifier.height(24.dp))
        // Like/Save buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleLike(postId) }) {
                Icon(
                    imageVector = if (viewModel.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (viewModel.isLiked) Color.Red else Color.Gray
                )
            }
            Text(text = viewModel.likeCount.toString(), fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { viewModel.toggleSave(postId) }) {
                Icon(
                    imageVector = if (viewModel.isSaved) Lucide.Bookmark else Lucide.BookmarkPlus,
                    contentDescription = "Save",
                    tint = if (viewModel.isSaved) Color(0xFF4CAF50) else Color.Gray
                )
            }
            Text(text = viewModel.saveCount.toString(), fontSize = 14.sp, color = Color.Gray)
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