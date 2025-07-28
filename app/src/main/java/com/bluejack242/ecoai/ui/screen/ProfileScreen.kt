package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.bluejack242.ecoai.ui.component.MediaCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.bluejack242.ecoai.viewmodel.ProfileViewModel
import com.bluejack242.ecoai.ui.component.ProfileCount

@Composable
fun ProfileScreen(
    navController: NavHostController,
    currentRoute: String = "profile",
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { viewModel.fetchProfile() }
    LaunchedEffect(selectedTab) { viewModel.fetchPostsForTab(selectedTab) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        if (!viewModel.isProfileLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                // Profile picture, username, edit button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!viewModel.profilePictureUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = viewModel.profilePictureUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "@${viewModel.username}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(viewModel.fullName.take(30), fontSize = 16.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Counts (dummy for now)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileCount("Following", viewModel.following.size)
                    ProfileCount("Followers", viewModel.followers.size)
                    ProfileCount("Likes", viewModel.likes)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Bio
                Text(
                    text = if (viewModel.bio.isBlank()) "Write a bio to help people discover you" else viewModel.bio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Logout button
                Button(
                    onClick = {
                        navController.navigate("edit_profile")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text("Edit Profile", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Tabs
                val selectedColor = Color(0xFF388E3C)
                val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = selectedColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = selectedColor
                        )
                    },
                    divider = {}
                ) {
                    listOf("Posts", "Saved", "Liked").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    color = if (selectedTab == index) selectedColor else unselectedColor
                                )
                            }
                        )
                    }
                }


                // Tab content
                when (selectedTab) {
                    0 -> {
                        if (viewModel.isLoadingPosts) {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(viewModel.ownPosts, key = { it.first }) { (postId, post) ->
                                    val mediaList =
                                        post["media"] as? List<Map<String, Any>> ?: emptyList()
                                    val firstMedia =
                                        mediaList.firstOrNull()?.get("url") as? String ?: ""
                                    val creatorName = (post["fullName"] as? String).orEmpty()
                                        .ifBlank { viewModel.fullName }
                                    val profilePictureUrl =
                                        (post["profilePictureUrl"] as? String).orEmpty()
                                            .ifBlank { viewModel.profilePictureUrl ?: "" }
                                    val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                    val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                    val liked =
                                        FirebaseAuth.getInstance().currentUser?.uid != null && likedBy.contains(
                                            FirebaseAuth.getInstance().currentUser?.uid
                                        )
                                    Box(Modifier.clickable { navController.navigate("post_detail/$postId") }) {
                                        MediaCard(
                                            imageUrl = firstMedia,
                                            title = post["headline"] as? String ?: "",
                                            fullName = creatorName,
                                            profilePictureUrl = profilePictureUrl,
                                            likes = likes,
                                            liked = liked
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        if (viewModel.isLoadingPosts) {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(viewModel.likedPosts, key = { it.first }) { (postId, post) ->
                                    val mediaList =
                                        post["media"] as? List<Map<String, Any>> ?: emptyList()
                                    val firstMedia =
                                        mediaList.firstOrNull()?.get("url") as? String ?: ""
                                    val creatorName = (post["fullName"] as? String).orEmpty()
                                        .ifBlank { viewModel.fullName }
                                    val profilePictureUrl =
                                        (post["profilePictureUrl"] as? String).orEmpty()
                                            .ifBlank { viewModel.profilePictureUrl ?: "" }
                                    val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                    val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                    val liked =
                                        FirebaseAuth.getInstance().currentUser?.uid != null && likedBy.contains(
                                            FirebaseAuth.getInstance().currentUser?.uid
                                        )
                                    Box(Modifier.clickable { navController.navigate("post_detail/$postId") }) {
                                        MediaCard(
                                            imageUrl = firstMedia,
                                            title = post["headline"] as? String ?: "",
                                            fullName = creatorName,
                                            profilePictureUrl = profilePictureUrl,
                                            likes = likes,
                                            liked = liked
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        if (viewModel.isLoadingPosts) {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(viewModel.savedPosts, key = { it.first }) { (postId, post) ->
                                    val mediaList =
                                        post["media"] as? List<Map<String, Any>> ?: emptyList()
                                    val firstMedia =
                                        mediaList.firstOrNull()?.get("url") as? String ?: ""
                                    val creatorName = (post["fullName"] as? String).orEmpty()
                                        .ifBlank { viewModel.fullName }
                                    val profilePictureUrl =
                                        (post["profilePictureUrl"] as? String).orEmpty()
                                            .ifBlank { viewModel.profilePictureUrl ?: "" }
                                    val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                    val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                    val liked =
                                        FirebaseAuth.getInstance().currentUser?.uid != null && likedBy.contains(
                                            FirebaseAuth.getInstance().currentUser?.uid
                                        )
                                    Box(Modifier.clickable { navController.navigate("post_detail/$postId") }) {
                                        MediaCard(
                                            imageUrl = firstMedia,
                                            title = post["headline"] as? String ?: "",
                                            fullName = creatorName,
                                            profilePictureUrl = profilePictureUrl,
                                            likes = likes,
                                            liked = liked
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfilePostsTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Your posts will appear here.")
    }
}

@Composable
fun ProfileSavedTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Your saved posts will appear here.")
    }
}

@Composable
fun ProfileLikedTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Your liked posts will appear here.")
    }
} 