package edu.bluejack24_2.ecoai.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import edu.bluejack24_2.ecoai.ui.component.BottomNavigationBar
import edu.bluejack24_2.ecoai.ui.component.FollowingPostCard
import edu.bluejack24_2.ecoai.ui.component.MediaCard
import edu.bluejack24_2.ecoai.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.model.mapPostToUiModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(kotlinx.coroutines.FlowPreview::class)
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
        val query = ""
        navController.navigate("search/$query")
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


            val topBarBg = MaterialTheme.colorScheme.surface
            val topBarText = MaterialTheme.colorScheme.onSurface
            val tabSelectedBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            val tabSelectedText = MaterialTheme.colorScheme.primary
            val tabUnselectedText = MaterialTheme.colorScheme.onSurfaceVariant
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                color = topBarBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Text(
                        text = "Eco AI",
                        color = topBarText,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tabTitles = listOf(LanguageManager.getString("for_you"), LanguageManager.getString("following"))
                            tabTitles.forEachIndexed { idx, title ->
                                val selected = selectedTab == idx
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) tabSelectedBg else Color.Transparent)
                                        .clickable { selectedTab = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title.uppercase(),
                                        color = if (selected) tabSelectedText else tabUnselectedText,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.25.sp,
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { setNavigateToSearch(true) },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = LanguageManager.getString("search"),
                                tint = topBarText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            state = gridState
                        ) {
                            items(forYouPosts, key = { it.first }) { (postId, post) ->
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
                                    commentsCount = commentsCount
                                )
                                Box(
                                    modifier = Modifier.clickable {
                                        navController.navigate("post_detail/$postId")
                                    }
                                ) {
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
                                @Suppress("UNCHECKED_CAST")
                                val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
                                val firstMedia = mediaList.firstOrNull()?.get("url") as? String ?: ""
                                val userId = post["userId"] as? String ?: ""
                                val fullName = post["fullName"] as? String ?: ""
                                val profilePictureUrl = post["profilePictureUrl"] as? String ?: ""
                                val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                val saves = (post["saves"] as? Long)?.toInt() ?: 0
                                val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                val savedBy = post["savedBy"] as? List<*> ?: emptyList<Any>()
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                val liked = currentUserId != null && likedBy.contains(currentUserId)
                                val saved = currentUserId != null && savedBy.contains(currentUserId)
                                val createdAt = post["createdAt"]

                                FollowingPostCard(
                                    postId = postId,
                                    profilePictureUrl = profilePictureUrl,
                                    userId = userId,
                                    fullName = fullName,
                                    title = post["headline"] as? String ?: "",
                                    imageUrl = firstMedia,
                                    mediaList = mediaList,
                                    caption = post["caption"] as? String ?: "",
                                    likes = likes,
                                    saves = saves,
                                    isLiked = liked,
                                    isSaved = saved,
                                    onLikeClick = { viewModel.toggleLike(it) },
                                    onSaveClick = { viewModel.toggleSave(it) },
                                    onCommentClick = { navController.navigate("post_detail/$it") },
                                    navController = navController,
                                    onDelete = { viewModel.deletePost(postId) },
                                    viewModel = viewModel,
                                    createdAt = createdAt
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}