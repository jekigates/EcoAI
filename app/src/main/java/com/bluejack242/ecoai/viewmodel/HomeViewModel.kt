package com.bluejack242.ecoai.viewmodel

import androidx.lifecycle.ViewModel
import com.bluejack242.ecoai.model.MediaItem
import com.bluejack242.ecoai.model.MediaType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Post(
    val id: String = "",
    val userId: String = "",
    val headline: String = "",
    val caption: String = "",
    val media: List<MediaItem> = emptyList(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
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
        firestore.collection("posts")
            .addSnapshotListener { result, error ->
                if (error != null) {
                    // Handle error (e.g., log it or show a Toast in UI)
                    return@addSnapshotListener
                }
                if (result != null) {
                    val postList = result.mapNotNull { document ->
                        try {
                            val mediaList = document.get("media") as? List<Map<String, Any>> ?: emptyList()
                            val mediaItems = mediaList.map { media ->
                                MediaItem(
                                    url = media["url"] as? String ?: "",
                                    type = MediaType.valueOf((media["type"] as? String) ?: "IMAGE")
                                )
                            }
                            val likedBy = (document.get("likedBy") as? List<*>)?.map { it.toString() } ?: emptyList()
                            Post(
                                id = document.id,
                                userId = document.getString("userId") ?: "",
                                headline = document.getString("headline") ?: "",
                                caption = document.getString("caption") ?: "",
                                media = mediaItems,
                                likes = document.getLong("likes")?.toInt() ?: 0,
                                likedBy = likedBy,
                                createdAt = document.getTimestamp("createdAt")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _posts.value = postList
                }
            }
    }
}