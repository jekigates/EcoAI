package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import edu.bluejack24_2.ecoai.utils.LanguageManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.saveable.rememberSaveable
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar

@Composable
fun SearchScreen(navController: NavHostController, query: String = "") {
    val db = FirebaseFirestore.getInstance()
    var searchText by rememberSaveable { mutableStateOf(query) }
    var topTags by remember { mutableStateOf(listOf<String>()) }
    var tagPosts by remember { mutableStateOf(mapOf<String, List<Pair<String, String>>>()) }
    var searchResults by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(searchText) {
        isLoading = true

        val postsSnapshot = db.collection("posts").get().await()
        val tagCount = mutableMapOf<String, Int>()
        val tagToPosts = mutableMapOf<String, MutableList<Triple<String, String, Int>>>()
        val results = mutableListOf<Pair<String, String>>()

        for (doc in postsSnapshot) {
            val postId = doc.id
            val caption = doc.getString("caption") ?: ""
            val headline = doc.getString("headline") ?: ""
            val likes = doc.getLong("likes")?.toInt() ?: 0
            @Suppress("UNCHECKED_CAST")
            val media = doc.get("media") as? List<Map<String, Any>>
            val thumbnail = media?.firstOrNull()?.get("url") as? String ?: ""

            if (searchText.isNotBlank()) {
                if (caption.contains(searchText, ignoreCase = true) || headline.contains(searchText, ignoreCase = true)) {
                    results.add(postId to thumbnail)
                }
            } else {
                val tagRegex = Regex("#[A-Za-z0-9_]+")
                for (tag in tagRegex.findAll(caption).map { it.value }) {
                    tagCount[tag] = (tagCount[tag] ?: 0) + 1
                    val postList = tagToPosts.getOrPut(tag) { mutableListOf() }
                    postList.add(Triple(postId, thumbnail, likes))
                }
            }
        }

        searchResults = results
        if (searchText.isBlank()) {
            val sortedTags = tagCount.entries.sortedByDescending { it.value }.map { it.key }
            topTags = sortedTags
            tagPosts = sortedTags.associateWith { tag ->
                tagToPosts[tag]?.sortedByDescending { it.third }
                    ?.take(5)?.map { it.first to it.second } ?: emptyList()
            }
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("search"),
            navController = navController
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = LanguageManager.getString("search"), tint = Color.Gray)
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text(LanguageManager.getString("search_placeholder")) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (searchText.isNotBlank()) {
                if (searchResults.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { (postId, thumbnailUrl) ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray)
                                    .clickable { navController.navigate("post_detail/$postId") }
                            ) {
                                if (thumbnailUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = thumbnailUrl,
                                        contentDescription = LanguageManager.getString("post_thumbnail"),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(topTags) { tag ->
                        Text(
                            text = tag,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.clickable {
                                searchText = tag
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tagPosts[tag]?.forEach { (postId, thumbnailUrl) ->
                                if (thumbnailUrl.isNotBlank()) {
                                    Box(
                                        Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.LightGray)
                                            .clickable { navController.navigate("post_detail/$postId") }
                                    ) {
                                        AsyncImage(
                                            model = thumbnailUrl,
                                            contentDescription = LanguageManager.getString("post_thumbnail"),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
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
