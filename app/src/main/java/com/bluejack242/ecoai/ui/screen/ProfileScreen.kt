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

@Composable
fun ProfileScreen(navController: NavHostController, currentRoute: String = "profile") {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var isProfileLoaded by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }

    // Fetch user profile from Firestore on first composition
    LaunchedEffect(user?.uid) {
        if (user != null && !isProfileLoaded) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                fullName = doc.getString("fullName") ?: ""
                username = doc.getString("username") ?: ""
                bio = doc.getString("bio") ?: ""
                profilePictureUrl = doc.getString("profilePictureUrl")
                isProfileLoaded = true
            }
        }
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var ownPosts by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var likedPosts by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var savedPosts by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var isLoadingPosts by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    fun fetchPostsForTab(tab: Int) {
        isLoadingPosts = true
        val db = FirebaseFirestore.getInstance()
        when (tab) {
            0 -> {
                db.collection("posts").whereEqualTo("userId", userId).get().addOnSuccessListener { result ->
                    ownPosts = result.documents.mapNotNull { doc -> doc.id to (doc.data as? Map<String, Any>) }.filter { it.second != null } as List<Pair<String, Map<String, Any>>>
                    isLoadingPosts = false
                }
            }
            1 -> {
                db.collection("posts").whereArrayContains("likedBy", userId ?: "").get().addOnSuccessListener { result ->
                    likedPosts = result.documents.mapNotNull { doc -> doc.id to (doc.data as? Map<String, Any>) }.filter { it.second != null } as List<Pair<String, Map<String, Any>>>
                    isLoadingPosts = false
                }
            }
            2 -> {
                db.collection("posts").whereArrayContains("savedBy", userId ?: "").get().addOnSuccessListener { result ->
                    savedPosts = result.documents.mapNotNull { doc -> doc.id to (doc.data as? Map<String, Any>) }.filter { it.second != null } as List<Pair<String, Map<String, Any>>>
                    isLoadingPosts = false
                }
            }
        }
    }

    LaunchedEffect(selectedTab) {
        fetchPostsForTab(selectedTab)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        if (!isProfileLoaded) {
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
                        if (!profilePictureUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profilePictureUrl,
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
                        Text("@${username}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(fullName.take(30), fontSize = 16.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { navController.navigate("edit_profile") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Counts (dummy for now)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileCount("Following", 123)
                    ProfileCount("Followers", 456)
                    ProfileCount("Likes", 789)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Bio
                Text(
                    text = if (bio.isBlank()) "Write a bio to help people discover you" else bio,
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
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("landing") {
                            popUpTo("landing") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Logout", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("Posts", "Saved", "Liked").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                // Tab content
                when (selectedTab) {
                    0 -> {
                        if (isLoadingPosts) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(ownPosts, key = { it.first }) { (postId, post) ->
                                    val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
                                    val firstMedia = mediaList.firstOrNull()?.get("url") as? String ?: ""
                                    val creatorName = (post["fullName"] as? String).orEmpty().ifBlank { fullName }
                                    val profilePictureUrl = (post["profilePictureUrl"] as? String).orEmpty().ifBlank { profilePictureUrl ?: "" }
                                    val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                    val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                    val liked = userId != null && likedBy.contains(userId)
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
                        if (isLoadingPosts) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(likedPosts, key = { it.first }) { (postId, post) ->
                                    val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
                                    val firstMedia = mediaList.firstOrNull()?.get("url") as? String ?: ""
                                    val creatorName = (post["fullName"] as? String).orEmpty().ifBlank { fullName }
                                    val profilePictureUrl = (post["profilePictureUrl"] as? String).orEmpty().ifBlank { profilePictureUrl ?: "" }
                                    val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                    val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                    val liked = userId != null && likedBy.contains(userId)
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
                        if (isLoadingPosts) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(savedPosts, key = { it.first }) { (postId, post) ->
                                    val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
                                    val firstMedia = mediaList.firstOrNull()?.get("url") as? String ?: ""
                                    val creatorName = (post["fullName"] as? String).orEmpty().ifBlank { fullName }
                                    val profilePictureUrl = (post["profilePictureUrl"] as? String).orEmpty().ifBlank { profilePictureUrl ?: "" }
                                    val likes = (post["likes"] as? Long)?.toInt() ?: 0
                                    val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
                                    val liked = userId != null && likedBy.contains(userId)
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
fun ProfileCount(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
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