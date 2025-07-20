package com.bluejack242.ecoai.ui.screen

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
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.bluejack242.ecoai.ui.component.MediaCard
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    navController: NavController,
    currentRoute: String
) {
    val viewModel: HomeViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("Following", "For You")

    val db = FirebaseFirestore.getInstance()
    val creatorInfoCache = remember { mutableStateMapOf<String, Pair<String, String?>>() } // userId -> (fullName, profilePictureUrl)
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // --- ADDED: State for following list ---
    var followingList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingFollowing by remember { mutableStateOf(true) }

    // Fetch following list when HomeScreen is composed or userId changes
    LaunchedEffect(userId) {
        if (userId != null) {
            isLoadingFollowing = true
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                followingList = (doc.get("following") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
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
        // Remove topBar so tabs are at the very top
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White
            ) {
                // Following tab
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = tabs[0],
                                color = if (selectedTab == 0) Color(0xFF388E3C) else Color.Gray
                            )
                        }
                    }
                )
                // For You tab
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tabs[1],
                                color = if (selectedTab == 1) Color(0xFF388E3C) else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { setNavigateToSearch(true) }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (selectedTab == 1) Color(0xFF388E3C) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- ADDED: Filter posts for Following tab ---
                val filteredPosts = if (selectedTab == 0) {
                    // Following tab: show only posts from followed users
                    if (isLoadingFollowing) {
                        emptyList()
                    } else {
                        posts.filter { followingList.contains(it.userId) }
                    }
                } else {
                    // For You tab: show all posts
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
                        Text("No posts from users you follow.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(filteredPosts) { post ->
                        val firstMedia = post.media.firstOrNull()

                        // Fetch creator info if not cached
                        val creatorInfo = creatorInfoCache[post.userId]
                        LaunchedEffect(post.userId) {
                            if (creatorInfo == null && post.userId.isNotBlank()) {
                                db.collection("users").document(post.userId).get().addOnSuccessListener { doc ->
                                    val fullName = doc.getString("fullName") ?: "Unknown"
                                    val profilePictureUrl = doc.getString("profilePictureUrl")
                                    creatorInfoCache[post.userId] = fullName.take(30) to profilePictureUrl
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
