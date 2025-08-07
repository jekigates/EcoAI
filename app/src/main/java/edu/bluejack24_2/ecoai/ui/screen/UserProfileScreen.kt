package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import edu.bluejack24_2.ecoai.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack24_2.ecoai.ui.component.ProfileCount
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import edu.bluejack24_2.ecoai.ui.component.MediaCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import edu.bluejack24_2.ecoai.model.mapPostToUiModel
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar

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
                val username = viewModel.username
                BackHeaderBar(
                    title = username.ifBlank { "" },
                    navController = navController
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isFollowing) Color.Gray else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            if (viewModel.isFollowing) edu.bluejack24_2.ecoai.utils.LanguageManager.getString("unfollow") else edu.bluejack24_2.ecoai.utils.LanguageManager.getString("follow"),
                            color = Color.White,
                            fontSize = 14.sp
                        )
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
                        var commentsCount by remember(postId) { mutableIntStateOf(0) }
                        LaunchedEffect(postId) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("posts").document(postId).collection("comments")
                                .addSnapshotListener { snapshot, _ ->
                                    commentsCount = snapshot?.size() ?: 0
                                }
                        }
                        val postUi = mapPostToUiModel(
                            postId = postId,
                            post = post,
                            fullNameFallback = viewModel.fullName,
                            profilePictureUrlFallback = viewModel.profilePictureUrl ?: "",
                            commentsCount = commentsCount
                        )
                        Box(Modifier.clickable { navController.navigate("post_detail/$postId") }) {
                            MediaCard(
                                imageUrl = postUi.imageUrl,
                                title = postUi.title,
                                fullName = postUi.fullName,
                                profilePictureUrl = postUi.profilePictureUrl,
                                likes = postUi.likes,
                                liked = postUi.liked,
                                saves = postUi.saves,
                                saved = postUi.saved,
                                commentsCount = postUi.commentsCount
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
} 