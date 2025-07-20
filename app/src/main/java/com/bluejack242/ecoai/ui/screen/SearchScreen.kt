package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun SearchScreen(navController: NavHostController, currentRoute: String = "search") {
    val db = FirebaseFirestore.getInstance()
    var searchText by remember { mutableStateOf("") }
    var topTags by remember { mutableStateOf(listOf<String>()) }
    var tagPosts by remember { mutableStateOf(mapOf<String, List<String>>()) } // tag -> list of post thumbnail urls
    var isLoading by remember { mutableStateOf(true) }

    // Fetch top tags and posts
    LaunchedEffect(Unit) {
        isLoading = true
        // Fetch all posts and extract tags
        val postsSnapshot = db.collection("posts").get().await()
        val tagCount = mutableMapOf<String, Int>()
        val tagToPosts = mutableMapOf<String, MutableList<Pair<String, Int>>>() // tag -> list of (postId, likes)
        for (doc in postsSnapshot) {
            val caption = doc.getString("caption") ?: ""
            val likes = doc.getLong("likes")?.toInt() ?: 0
            val media = doc.get("media") as? List<Map<String, Any>>
            val thumbnail = media?.firstOrNull()?.get("url") as? String ?: ""
            val tagRegex = Regex("#[A-Za-z0-9_]+")
            for (tag in tagRegex.findAll(caption).map { it.value }) {
                tagCount[tag] = (tagCount[tag] ?: 0) + 1
                val postList = tagToPosts.getOrPut(tag) { mutableListOf() }
                postList.add(Pair(thumbnail, likes))
            }
        }
        val sortedTags = tagCount.entries.sortedByDescending { it.value }.take(5).map { it.key }
        topTags = sortedTags
        tagPosts = sortedTags.associateWith { tag ->
            tagToPosts[tag]?.sortedByDescending { it.second }?.take(5)?.map { it.first } ?: emptyList()
        }
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Top bar with search input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search tags or posts") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { /* Search action, keep empty for now */ }) {
                    Text("Search")
                }
            }
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(topTags) { tag ->
                        Text(tag, color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tagPosts[tag]?.forEach { thumbnailUrl ->
                                if (thumbnailUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = thumbnailUrl,
                                        contentDescription = "Post Thumbnail",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.LightGray)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
} 