package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.bluejack242.ecoai.viewmodel.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.clickable
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.bluejack242.ecoai.ui.component.MediaCard
import com.bluejack242.ecoai.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    navController: NavController,
    currentRoute: String
) {
    val viewModel: HomeViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf(LanguageManager.getString("following"), LanguageManager.getString("for_you"))

    val db = FirebaseFirestore.getInstance()
    val creatorInfoCache =
        remember { mutableStateMapOf<String, Pair<String, String?>>() } // userId -> (fullName, profilePictureUrl)
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // --- ADDED: State for following list ---
    var followingList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingFollowing by remember { mutableStateOf(true) }

    // Fetch following list when HomeScreen is composed or userId changes
    LaunchedEffect(userId) {
        if (userId != null) {
            isLoadingFollowing = true
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                followingList =
                    (doc.get("following") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                isLoadingFollowing = false
            }.addOnFailureListener {
                followingList = emptyList()
                isLoadingFollowing = false
            }
        } else {
            followingList = emptyList()
            isLoadingFollowing = false
        }
    }

    val (navigateToSearch, setNavigateToSearch) = remember { mutableStateOf(false) }
    if (navigateToSearch) {
        (navController as? NavHostController)?.navigate("search")
        setNavigateToSearch(false)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController as NavHostController,
                currentRoute = currentRoute
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                                text = tabs[0],
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
                                text = tabs[1],
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredPosts = if (selectedTab == 0) {
                    if (isLoadingFollowing) {
                        emptyList()
                    } else {
                        posts.filter { followingList.contains(it.userId) }
                    }
                } else {
                    posts
                }
                if (selectedTab == 0 && isLoadingFollowing) {
                    item(span = { GridItemSpan(2) }) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (selectedTab == 0 && filteredPosts.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            LanguageManager.getString("no_posts_following"),
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(filteredPosts) { post ->
                        val firstMedia = post.media.firstOrNull()

                        // Fetch creator info if not cached
                        val creatorInfo = creatorInfoCache[post.userId]
                        LaunchedEffect(post.userId) {
                            if (creatorInfo == null && post.userId.isNotBlank()) {
                                db.collection("users").document(post.userId).get()
                                    .addOnSuccessListener { doc ->
                                        val fullName = doc.getString("fullName") ?: LanguageManager.getString("unknown_user")
                                        val profilePictureUrl = doc.getString("profilePictureUrl")
                                        creatorInfoCache[post.userId] =
                                            fullName.take(30) to profilePictureUrl
                                    }
                            }
                        }
                        val liked = userId != null && post.likedBy.contains(userId)
                        Box(modifier = Modifier.clickable {
                            (navController as? NavHostController)?.navigate("post_detail/${post.id}")
                        }) {
                            MediaCard(
                                imageUrl = firstMedia?.url ?: "No media",
                                title = post.headline,
                                fullName = creatorInfo?.first ?: "...",
                                profilePictureUrl = creatorInfo?.second,
                                likes = post.likes,
                                liked = liked
                            )
                        }
                    }
                }
            }
        }
    }
}
