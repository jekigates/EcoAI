package com.bluejack242.ecoai.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var forYouPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
        private set
    var followingPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var hasMorePosts by mutableStateOf(true)
        private set

    private var currentPage = 0
    private val pageSize = 10

    init {
        loadInitialPosts()
    }

    private fun loadInitialPosts() {
        viewModelScope.launch {
            isLoading = true
            currentPage = 0
            loadForYouPosts()
            loadFollowingPosts()
            isLoading = false
        }
    }

    fun loadMorePosts() {
        if (!isLoading && hasMorePosts) {
            viewModelScope.launch {
                isLoading = true
                currentPage++
                loadForYouPosts(append = true)
                isLoading = false
            }
        }
    }

    private suspend fun loadForYouPosts(append: Boolean = false) {
        try {
            val postsSnapshot = firestore.collection("posts")
                .orderBy("createdAt")
                .limit((pageSize * (currentPage + 1)).toLong())
                .get().await()

            val posts = mutableListOf<Pair<String, Map<String, Any>>>()

            for (postDoc in postsSnapshot.documents.reversed()) {
                val postData = postDoc.data ?: continue
                val userId = postData["userId"] as? String ?: continue

                val userDoc = firestore.collection("users").document(userId).get().await()
                val userData = userDoc.data ?: continue

                val enrichedPost = postData.toMutableMap()
                enrichedPost["username"] = userData["username"] ?: ""
                enrichedPost["fullName"] = userData["fullName"] ?: ""
                enrichedPost["profilePictureUrl"] = userData["profilePictureUrl"] ?: ""

                val commentsSnapshot = firestore.collection("posts").document(postDoc.id)
                    .collection("comments")
                    .get().await()

                val comments = commentsSnapshot.documents.mapNotNull { commentDoc ->
                    val commentData = commentDoc.data ?: return@mapNotNull null
                    val commentUserId = commentData["userId"] as? String ?: return@mapNotNull null

                    val commentUserDoc = firestore.collection("users").document(commentUserId).get().await()
                    val commentUserData = commentUserDoc.data ?: return@mapNotNull null

                    val enrichedComment = commentData.toMutableMap()
                    enrichedComment["username"] = commentUserData["username"] ?: ""
                    enrichedComment["profilePictureUrl"] = commentUserData["profilePictureUrl"] ?: ""
                    enrichedComment
                }

                enrichedPost["comments"] = comments.sortedByDescending {
                    (it["likes"] as? Long) ?: 0
                }.take(3)

                posts.add(postDoc.id to enrichedPost)
            }

            if (append) {
                val newPosts = posts.filter { newPost ->
                    forYouPosts.none { existingPost -> existingPost.first == newPost.first }
                }
                forYouPosts = forYouPosts + newPosts
                hasMorePosts = newPosts.isNotEmpty()
            } else {
                forYouPosts = posts
                hasMorePosts = posts.size >= pageSize
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun loadFollowingPosts() {
        val currentUser = auth.currentUser ?: return

        try {
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val following = userDoc.get("following") as? List<String> ?: emptyList()

            if (following.isEmpty()) {
                followingPosts = emptyList()
                return
            }

            val posts = mutableListOf<Pair<String, Map<String, Any>>>()

            for (followedUserId in following) {
                val userPostsSnapshot = firestore.collection("posts")
                    .whereEqualTo("userId", followedUserId)
                    .get().await()

                for (postDoc in userPostsSnapshot.documents) {
                    val postData = postDoc.data ?: continue

                    val userDoc = firestore.collection("users").document(followedUserId).get().await()
                    val userData = userDoc.data ?: continue

                    val enrichedPost = postData.toMutableMap()
                    enrichedPost["username"] = userData["username"] ?: ""
                    enrichedPost["fullName"] = userData["fullName"] ?: ""
                    enrichedPost["profilePictureUrl"] = userData["profilePictureUrl"] ?: ""

                    val commentsSnapshot = firestore.collection("posts").document(postDoc.id)
                        .collection("comments")
                        .get().await()

                    val comments = commentsSnapshot.documents.mapNotNull { commentDoc ->
                        val commentData = commentDoc.data ?: return@mapNotNull null
                        val commentUserId = commentData["userId"] as? String ?: return@mapNotNull null

                        val commentUserDoc = firestore.collection("users").document(commentUserId).get().await()
                        val commentUserData = commentUserDoc.data ?: return@mapNotNull null

                        val enrichedComment = commentData.toMutableMap()
                        enrichedComment["username"] = commentUserData["username"] ?: ""
                        enrichedComment["profilePictureUrl"] = commentUserData["profilePictureUrl"] ?: ""
                        enrichedComment
                    }

                    enrichedPost["comments"] = comments.sortedByDescending {
                        (it["likes"] as? Long) ?: 0
                    }.take(3)

                    posts.add(postDoc.id to enrichedPost)
                }
            }

            followingPosts = posts.sortedByDescending { (_, post) ->
                (post["createdAt"] as? Long) ?: 0
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun toggleLike(postId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val postRef = firestore.collection("posts").document(postId)
                val postDoc = postRef.get().await()
                val postData = postDoc.data ?: return@launch

                val likedBy = (postData["likedBy"] as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toMutableList()
                    ?: mutableListOf()

                val likes = (postData["likes"] as? Long)?.toInt() ?: 0

                if (likedBy.contains(currentUser.uid)) {
                    likedBy.remove(currentUser.uid)
                    postRef.update(
                        mapOf(
                            "likedBy" to likedBy,
                            "likes" to maxOf(0, likes - 1)
                        )
                    )
                } else {
                    likedBy.add(currentUser.uid)
                    postRef.update(
                        mapOf(
                            "likedBy" to likedBy,
                            "likes" to likes + 1
                        )
                    )
                }

                updateLocalPosts(
                    postId,
                    likedBy,
                    if (likedBy.contains(currentUser.uid)) likes + 1 else maxOf(0, likes - 1)
                )
            } catch (e: Exception) {
            }
        }
    }


    fun toggleSave(postId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val postRef = firestore.collection("posts").document(postId)
                val postDoc = postRef.get().await()
                val postData = postDoc.data ?: return@launch

                val savedBy = (postData["savedBy"] as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toMutableList()
                    ?: mutableListOf()

                val saves = (postData["saves"] as? Long)?.toInt() ?: 0

                if (savedBy.contains(currentUser.uid)) {
                    savedBy.remove(currentUser.uid)
                    postRef.update(
                        mapOf(
                            "savedBy" to savedBy,
                            "saves" to maxOf(0, saves - 1)
                        )
                    )
                } else {
                    savedBy.add(currentUser.uid)
                    postRef.update(
                        mapOf(
                            "savedBy" to savedBy,
                            "saves" to saves + 1
                        )
                    )
                }
            } catch (e: Exception) {
            }
        }
    }


    private fun updateLocalPosts(postId: String, likedBy: List<*>, newLikes: Int) {
        forYouPosts = forYouPosts.map { (id, post) ->
            if (id == postId) {
                val updatedPost = post.toMutableMap()
                updatedPost["likedBy"] = likedBy
                updatedPost["likes"] = newLikes.toLong()
                id to updatedPost
            } else {
                id to post
            }
        }

        followingPosts = followingPosts.map { (id, post) ->
            if (id == postId) {
                val updatedPost = post.toMutableMap()
                updatedPost["likedBy"] = likedBy
                updatedPost["likes"] = newLikes.toLong()
                id to updatedPost
            } else {
                id to post
            }
        }
    }
}
