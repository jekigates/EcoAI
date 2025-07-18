package com.bluejack242.ecoai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluejack242.ecoai.model.MediaItem
import com.bluejack242.ecoai.model.MediaType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Post(
    val id: String = "",
    val userId: String = "",
    val headline: String = "",
    val caption: String = "",
    val media: List<MediaItem> = emptyList(),
    val likes: Int = 0,
    val createdAt: com.google.firebase.Timestamp? = null,
    val username: String = ""
)

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            firestore.collection("posts")
                .get()
                .addOnSuccessListener { result ->
                    val postList = result.mapNotNull { document ->
                        try {
                            val mediaList = document.get("media") as? List<Map<String, Any>> ?: emptyList()
                            val mediaItems = mediaList.map { media ->
                                MediaItem(
                                    url = media["url"] as? String ?: "",
                                    type = MediaType.valueOf((media["type"] as? String) ?: "IMAGE")
                                )
                            }
                            Post(
                                id = document.id,
                                userId = document.getString("userId") ?: "",
                                headline = document.getString("headline") ?: "",
                                caption = document.getString("caption") ?: "",
                                media = mediaItems,
                                likes = document.getLong("likes")?.toInt() ?: 0,
                                createdAt = document.getTimestamp("createdAt"),
                                username = document.getString("username") ?: "Unknown"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _posts.value = postList
                }
                .addOnFailureListener { e ->
                    // Handle error (e.g., log it or show a Toast in UI)
                }
        }
    }
}