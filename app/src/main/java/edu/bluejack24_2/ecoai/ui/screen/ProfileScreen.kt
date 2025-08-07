package edu.bluejack24_2.ecoai.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import edu.bluejack24_2.ecoai.ui.component.BottomNavigationBar
import edu.bluejack24_2.ecoai.ui.component.MediaCard
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import edu.bluejack24_2.ecoai.viewmodel.ProfileViewModel
import edu.bluejack24_2.ecoai.ui.component.ProfileCount
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.model.mapPostToUiModel
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.getValue


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ProfileScreen(
    navController: NavHostController,
    currentRoute: String = "profile",
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { viewModel.fetchProfile() }
    LaunchedEffect(selectedTab) {
        viewModel.resetPostsForTab(selectedTab)
        viewModel.fetchPostsForTab(selectedTab)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        val backgroundColor = MaterialTheme.colorScheme.background
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
        val indicatorColor = MaterialTheme.colorScheme.primary
        if (!viewModel.isProfileLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
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
                            .background(MaterialTheme.colorScheme.surfaceVariant),
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
                                tint = onSurfaceVariantColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.username.isNotBlank()) {
                            Text(
                                "@${viewModel.username}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = onSurfaceColor
                            )
                            Text(
                                viewModel.fullName.take(30),
                                fontSize = 16.sp,
                                color = onSurfaceVariantColor
                            )
                        } else {
                            Text(
                                viewModel.fullName.take(30),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = onSurfaceColor
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = onSurfaceVariantColor)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileCount(LanguageManager.getString("following"), viewModel.following.size) {
                        if (viewModel.following.isNotEmpty()) {
                            val ids = viewModel.following.joinToString(",")
                            navController.navigate("following_list/$ids")
                        }
                    }
                    ProfileCount(LanguageManager.getString("followers"), viewModel.followers.size) {
                        if (viewModel.followers.isNotEmpty()) {
                            val ids = viewModel.followers.joinToString(",")
                            navController.navigate("followers_list/$ids")
                        }
                    }
                    ProfileCount(LanguageManager.getString("likes"), viewModel.likes)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Bio
                Text(
                    text = viewModel.bio.ifBlank { LanguageManager.getString("write_bio_placeholder") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(LanguageManager.getString("edit_profile"), color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Tabs

                val selectedColor = indicatorColor

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = selectedColor,
                    indicator = { tabPositions ->
                        val tabWidth = tabPositions[selectedTab].width
                        val indicatorWidth = 32.dp
                        val targetOffset = tabPositions[selectedTab].left + (tabWidth - indicatorWidth) / 2
                        val animatedOffset by animateDpAsState(targetValue = targetOffset, label = "TabIndicatorOffset")
                        Box(
                            Modifier
                                .wrapContentSize(Alignment.BottomStart)
                                .offset(x = animatedOffset)
                                .width(indicatorWidth)
                                .height(3.dp)
                                .background(selectedColor, RoundedCornerShape(1.5.dp))
                        )
                    },
                    divider = {}
                ) {
                    val tabIcons = listOf(
                        Icons.Default.GridOn,      // My Posts
                        Icons.Default.Bookmark,    // Saved
                        Icons.Default.Favorite     // Liked
                    )
                    tabIcons.forEachIndexed { index, icon ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selectedTab == index) selectedColor else onSurfaceVariantColor
                                )
                            },
                            modifier = Modifier.background(Color.Transparent)
                        )
                    }
                }


                when (selectedTab) {
                    0 -> {
                        if (viewModel.isLoadingPosts) {
                            Box(
                                Modifier.fillMaxSize().background(backgroundColor),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(backgroundColor),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(viewModel.ownPosts, key = { it.first }) { (postId, post) ->
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

                    1 -> {
                        if (viewModel.isLoadingPosts) {
                            Box(
                                Modifier.fillMaxSize().background(backgroundColor),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(backgroundColor),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(viewModel.savedPosts, key = { it.first }) { (postId, post) ->
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

                    2 -> {
                        if (viewModel.isLoadingPosts) {
                            Box(
                                Modifier.fillMaxSize().background(backgroundColor),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(backgroundColor),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(viewModel.likedPosts, key = { it.first }) { (postId, post) ->
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
                }
            }
        }
    }
}

