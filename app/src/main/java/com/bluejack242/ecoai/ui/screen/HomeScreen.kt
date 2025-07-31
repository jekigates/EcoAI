package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.bluejack242.ecoai.ui.component.FollowingPostCard
import com.bluejack242.ecoai.ui.component.MediaCard
import com.bluejack242.ecoai.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.bluejack242.ecoai.utils.LanguageManager
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeScreen(
    navController: NavHostController,
    currentRoute: String = "home",
) {
    val viewModel: HomeViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    val forYouPosts by remember { derivedStateOf { viewModel.forYouPosts } }
    val followingPosts by remember { derivedStateOf { viewModel.followingPosts } }

    LaunchedEffect(selectedTab, gridState, listState) {
        if (selectedTab == 0) {
            snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .distinctUntilChanged()
                .debounce(300)
                .collect { index ->
                    if (index != null && index >= forYouPosts.size - 3) {
                        viewModel.loadForYouPosts(append = true)
                    }
                }
        } else {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .distinctUntilChanged()
                .debounce(300)
                .collect { index ->
                    if (index != null && index >= followingPosts.size - 3) {
                        viewModel.loadFollowingPosts(append = true)
                    }
                }
        }
    }



    val (navigateToSearch, setNavigateToSearch) = remember { mutableStateOf(false) }
    if (navigateToSearch) {
        navController.navigate("search")
        setNavigateToSearch(false)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.size(40.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .fillMaxWidth(0.4f)
                                .align(Alignment.Bottom)
                                .height(2.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                    },
                    divider = {}
                ) {
                    // Following tab
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = LanguageManager.getString("for_you"),
                                color = if (selectedTab == 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Gray
                            )
                        }
                    )

                    // For You tab
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text =
                                    LanguageManager.getString("following"),
                                color = if (selectedTab == 1)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Gray
                            )
                        }
                    )
                }

                IconButton(onClick = { setNavigateToSearch(true) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = LanguageManager.getString("search"),
                        tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    if (viewModel.isLoadingForYou && forYouPosts.isEmpty()) {
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
                            state = gridState
                        ) {
                            items(forYouPosts, key = { it.first }) { (postId, post) ->
                                val mediaList =
                                    post["media"] as? List<Map<String, Any>> ?: emptyList()
                                val firstMedia =
                                    mediaList.firstOrNull()?.get("url") as? String ?: ""
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

                            if (viewModel.isLoadingForYou) {
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
                    if (viewModel.isLoadingFollowing && followingPosts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (followingPosts.isEmpty()) {
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
                            state = listState,
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(followingPosts, key = { it.first }) { (postId, post) ->
                                val mediaList =
                                    post["media"] as? List<Map<String, Any>> ?: emptyList()
                                val firstMedia =
                                    mediaList.firstOrNull()?.get("url") as? String ?: ""
                                val username = post["username"] as? String ?: ""
                                val profilePictureUrl = post["profilePictureUrl"] as? String ?: ""
                                val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                val saves = (post["saves"] as? Long)?.toInt() ?: 0
                                val comments =
                                    post["comments"] as? List<Map<String, Any>> ?: emptyList()
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
