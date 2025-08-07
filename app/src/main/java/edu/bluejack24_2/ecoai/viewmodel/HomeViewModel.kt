package edu.bluejack24_2.ecoai.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack24_2.ecoai.utils.sendNotificationWithType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private var newPostsListener: ListenerRegistration? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var forYouPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
        private set
    var followingPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
        private set

    var isLoadingForYou by mutableStateOf(false)
        private set
    var isLoadingFollowing by mutableStateOf(false)
        private set

    private var lastForYouSnapshot: DocumentSnapshot? = null
    private var lastFollowingSnapshot: DocumentSnapshot? = null

    private val pageSize = 3

    private val _commentsMap = mutableMapOf<String, List<Map<String, Any>>>()
    val commentsMap: Map<String, List<Map<String, Any>>>
        get() = _commentsMap

    init {
        loadInitialPosts()
        listenForNewPosts()
    }
    private fun listenForNewPosts() {
        newPostsListener?.remove()
        newPostsListener = firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val postDoc = change.document
                        viewModelScope.launch {
                        val postData = postDoc.data
                        val userId = postData["userId"] as? String ?: return@launch
                        val authorDoc = firestore.collection("users").document(userId).get().await()
                        val authorData = authorDoc.data ?: return@launch
                        val enriched = postData.toMutableMap().apply {
                            put("username", authorData["username"] ?: "")
                            put("fullName", authorData["fullName"] ?: "")
                            put("profilePictureUrl", authorData["profilePictureUrl"] ?: "")
                        }
                        val newPair = postDoc.id to enriched
                        if (forYouPosts.none { it.first == postDoc.id }) {
                            forYouPosts = listOf(newPair) + forYouPosts
                        }
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                            @Suppress("UNCHECKED_CAST")
                            val following = userDoc.get("following") as? List<String> ?: emptyList()
                            val followingWithSelf = (following + currentUser.uid).distinct().take(10)
                            if (followingWithSelf.contains(userId)) {
                                if (followingPosts.none { it.first == postDoc.id }) {
                                    followingPosts = listOf(newPair) + followingPosts
                                }
                            }
                        }
                        }
                    }
                }
            }
    }
    override fun onCleared() {
        super.onCleared()
        newPostsListener?.remove()
    }

    private fun loadInitialPosts() {
        viewModelScope.launch {
            loadForYouPosts()
            loadFollowingPosts()
        }
    }

    fun loadForYouPosts(append: Boolean = false) {
        if (isLoadingForYou) return
        viewModelScope.launch {
            isLoadingForYou = true

            var query = firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
            if (append && lastForYouSnapshot != null) {
                query = query.startAfter(lastForYouSnapshot!!)
            }

            val snapshot = query.get().await()
            if (snapshot.documents.isNotEmpty()) {
                lastForYouSnapshot = snapshot.documents.last()
            }

            val newPosts = enrichPosts(snapshot.documents)
            forYouPosts = if (append) forYouPosts + newPosts else newPosts

            isLoadingForYou = false
        }
    }

    fun loadFollowingPosts(append: Boolean = false) {
        if (isLoadingFollowing) return
        viewModelScope.launch {
            isLoadingFollowing = true

            val currentUser = auth.currentUser ?: return@launch
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            @Suppress("UNCHECKED_CAST")
            val following = userDoc.get("following") as? List<String> ?: emptyList()

            val followingWithSelf = (following + currentUser.uid).distinct().take(10)

            if (followingWithSelf.isEmpty()) {
                followingPosts = emptyList()
                isLoadingFollowing = false
                return@launch
            }

            var query = firestore.collection("posts")
                .whereIn("userId", followingWithSelf)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
            if (append && lastFollowingSnapshot != null) {
                query = query.startAfter(lastFollowingSnapshot!!)
            }

            val snapshot = query.get().await()
            if (snapshot.documents.isNotEmpty()) {
                lastFollowingSnapshot = snapshot.documents.last()
            }

            val newPosts = enrichPosts(snapshot.documents)
            followingPosts = if (append) followingPosts + newPosts else newPosts

            snapshot.documents.forEach { postDoc ->
                loadComments(postDoc.id)
            }

            isLoadingFollowing = false
        }
    }

    fun loadComments(postId: String) {
        firestore.collection("posts").document(postId).collection("comments")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                snapshot?.let {
                    viewModelScope.launch {
                        val enrichedComments = enrichComments(it.documents)
                        _commentsMap[postId] = enrichedComments
                    }
                } ?: run { _commentsMap[postId] = emptyList() }
            }
    }

    private suspend fun enrichPosts(docs: List<DocumentSnapshot>) =
        docs.mapNotNull { postDoc ->
            val postData = postDoc.data ?: return@mapNotNull null
            val userId = postData["userId"] as? String ?: return@mapNotNull null
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userData = userDoc.data ?: return@mapNotNull null

            val enriched = postData.toMutableMap().apply {
                put("username", userData["username"] ?: "")
                put("fullName", userData["fullName"] ?: "")
                put("profilePictureUrl", userData["profilePictureUrl"] ?: "")
            }
            postDoc.id to enriched
        }

    private suspend fun enrichComments(commentDocs: List<DocumentSnapshot>): List<Map<String, Any>> =
        commentDocs.mapNotNull { commentDoc ->
            val commentData = commentDoc.data ?: return@mapNotNull null
            val userId = commentData["userId"] as? String ?: return@mapNotNull null
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userData = userDoc.data ?: return@mapNotNull null

            commentData.toMutableMap().apply {
                put("username", userData["username"] ?: "Unknown")
                put("profilePictureUrl", userData["profilePictureUrl"] ?: "")
            }.plus("id" to commentDoc.id)
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
                    sendNotificationWithType(
                        fromUserId = currentUser.uid,
                        toUserId = postData["userId"].toString(),
                        type = "like",
                        postId = postId
                    )
                }

                updateLocalPosts(
                    postId,
                    likedBy,
                    if (likedBy.contains(currentUser.uid)) likes + 1 else maxOf(0, likes - 1)
                )
            } catch (e: Exception) {
                println("[HomeViewModel] Error in toggleLike: ${e.message}")
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
                println("[HomeViewModel] Error in toggleSave: ${e.message}")
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

    fun deletePost(postId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val postRef = firestore.collection("posts").document(postId)
                val postDoc = postRef.get().await()
                val postData = postDoc.data ?: return@launch

                if (postData["userId"] == currentUser.uid) {
                    postRef.delete().await()

                    forYouPosts = forYouPosts.filter { it.first != postId }
                    followingPosts = followingPosts.filter { it.first != postId }
                }
            } catch (e: Exception) {
                println("[HomeViewModel] Error in deletePost: ${e.message}")
            }
        }
    }
}