package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.bluejack242.ecoai.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.bluejack242.ecoai.ui.component.ProfileCount
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.bluejack242.ecoai.ui.component.MediaCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn

@Composable
fun UserProfileScreen(userId: String, navController: NavHostController, viewModel: UserProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(userId) { viewModel.setUserIdAndFetch(userId) }

    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "@${viewModel.username}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!viewModel.profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = viewModel.profilePictureUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(100.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(viewModel.fullName.take(1).uppercase(), fontSize = 40.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = viewModel.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        }
        item { Spacer(Modifier.height(4.dp)) }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = viewModel.bio,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileCount("Following", viewModel.following.size) {
                    if (viewModel.following.isNotEmpty()) {
                        val ids = viewModel.following.joinToString(",")
                        navController.navigate("following_list/$ids")
                    }
                }
                ProfileCount("Followers", viewModel.followers.size) {
                    if (viewModel.followers.isNotEmpty()) {
                        val ids = viewModel.followers.joinToString(",")
                        navController.navigate("followers_list/$ids")
                    }
                }
                ProfileCount("Likes", viewModel.likes)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            if (currentUserId != null && currentUserId != userId) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { viewModel.toggleFollow() },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (viewModel.isFollowing) "Unfollow" else "Follow", color = if (viewModel.isFollowing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Text("Posts", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        item {
            if (viewModel.isLoading && viewModel.userPosts.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (viewModel.userPosts.isEmpty()) {
                Text("No posts yet", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 1000.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.userPosts, key = { it.first }) { (postId, post) ->
                        val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
                        val firstMedia = mediaList.firstOrNull()?.get("url") as? String ?: ""
                        val creatorName = (post["fullName"] as? String).orEmpty().ifBlank { viewModel.fullName }
                        val profilePictureUrl = (post["profilePictureUrl"] as? String).orEmpty().ifBlank { viewModel.profilePictureUrl ?: "" }
                        val likes = (post["likes"] as? Long)?.toInt() ?: 0
                        val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        val liked = currentUserId != null && likedBy.contains(currentUserId)
                        val savedBy = post["savedBy"] as? List<*> ?: emptyList<Any>()
                        val saved = currentUserId != null && savedBy.contains(currentUserId)
                        val saves = savedBy.size
                        var commentsCount by remember(postId) { mutableStateOf(0) }
                        LaunchedEffect(postId) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("posts").document(postId).collection("comments")
                                .addSnapshotListener { snapshot, _ ->
                                    commentsCount = snapshot?.size() ?: 0
                                }
                        }
                        Box(Modifier.clickable { navController.navigate("post_detail/$postId") }) {
                            MediaCard(
                                imageUrl = firstMedia,
                                title = post["headline"] as? String ?: "",
                                fullName = creatorName,
                                profilePictureUrl = profilePictureUrl,
                                likes = likes,
                                liked = liked,
                                saves = saves,
                                saved = saved,
                                commentsCount = commentsCount
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
} 