package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.bluejack242.ecoai.ui.component.MediaCard
import com.bluejack242.ecoai.ui.component.FollowingPostCard
import com.bluejack242.ecoai.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.bluejack242.ecoai.utils.LanguageManager

@Composable
fun HomeScreen(
    navController: NavHostController,
    currentRoute: String = "home",
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val viewModel: HomeViewModel = viewModel()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= viewModel.forYouPosts.size - 3 &&
                    selectedTab == 0) {
                    viewModel.loadMorePosts()
                }
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                listOf(
                    LanguageManager.getString("for_you"),
                    LanguageManager.getString("following")
                ).forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    if (viewModel.isLoading && viewModel.forYouPosts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            state = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                        ) {
                            items(viewModel.forYouPosts, key = { it.first }) { (postId, post) ->
                                val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
                                val firstMedia = mediaList.firstOrNull()?.get("url") as? String ?: ""
                                val username = post["username"] as? String ?: ""
                                val profilePictureUrl = post["profilePictureUrl"] as? String ?: ""
                                val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                val liked = FirebaseAuth.getInstance().currentUser?.uid != null &&
                                        likedBy.contains(FirebaseAuth.getInstance().currentUser?.uid)

                                Box(
                                    modifier = Modifier.clickable {
                                        navController.navigate("post_detail/$postId")
                                    }
                                ) {
                                    MediaCard(
                                        imageUrl = firstMedia,
                                        title = post["headline"] as? String ?: "",
                                        fullName = "@$username",
                                        profilePictureUrl = profilePictureUrl,
                                        likes = likes,
                                        liked = liked
                                    )
                                }
                            }

                            if (viewModel.isLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (viewModel.isLoading && viewModel.followingPosts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (viewModel.followingPosts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = LanguageManager.getString("no_posts_following"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            items(viewModel.followingPosts, key = { it.first }) { (postId, post) ->
                                val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
                                val firstMedia = mediaList.firstOrNull()?.get("url") as? String ?: ""
                                val username = post["username"] as? String ?: ""
                                val profilePictureUrl = post["profilePictureUrl"] as? String ?: ""
                                val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                val saves = (post["saves"] as? Long)?.toInt() ?: 0
                                val comments = post["comments"] as? List<Map<String, Any>> ?: emptyList()
                                val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                val savedBy = post["savedBy"] as? List<*> ?: emptyList<Any>()
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                val liked = currentUserId != null && likedBy.contains(currentUserId)
                                val saved = currentUserId != null && savedBy.contains(currentUserId)

                                FollowingPostCard(
                                    postId = postId,
                                    profilePictureUrl = profilePictureUrl,
                                    username = username,
                                    title = post["headline"] as? String ?: "",
                                    imageUrl = firstMedia,
                                    caption = post["caption"] as? String ?: "",
                                    likes = likes,
                                    comments = comments,
                                    saves = saves,
                                    isLiked = liked,
                                    isSaved = saved,
                                    onLikeClick = { viewModel.toggleLike(it) },
                                    onSaveClick = { viewModel.toggleSave(it) },
                                    onCommentClick = { navController.navigate("post_detail/$it") },
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
