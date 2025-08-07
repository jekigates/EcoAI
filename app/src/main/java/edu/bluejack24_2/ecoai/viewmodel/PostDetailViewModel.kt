package edu.bluejack24_2.ecoai.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableIntStateOf
import edu.bluejack24_2.ecoai.utils.sendNotificationWithType
import kotlinx.coroutines.tasks.await

class PostDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    var isLoading by mutableStateOf(true)
    var post by mutableStateOf<Map<String, Any>?>(null)
    var creator by mutableStateOf<Map<String, Any>?>(null)
    var isFollowing by mutableStateOf(false)
    var isLiked by mutableStateOf(false)
    var isSaved by mutableStateOf(false)
    var likeCount by mutableIntStateOf(0)
    var saveCount by mutableIntStateOf(0)
    var showBottomSheet by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)
    var currentPagerIndex by mutableIntStateOf(0)
    var showPagerIndicator by mutableStateOf(false)
    private var pagerIndicatorJob: Job? = null

    fun loadPost(postId: String) {
        isLoading = true
        db.collection("posts").document(postId).get().addOnSuccessListener { doc ->
            post = doc.data
            val creatorId = doc.getString("userId") ?: ""
            likeCount = (doc.get("likedBy") as? List<*>)?.size ?: 0
            saveCount = (doc.get("savedBy") as? List<*>)?.size ?: 0
            isLiked = userId != null && (doc.get("likedBy") as? List<*>)?.contains(userId) == true
            isSaved = userId != null && (doc.get("savedBy") as? List<*>)?.contains(userId) == true
            if (creatorId.isNotBlank()) {
                db.collection("users").document(creatorId).get().addOnSuccessListener { userDoc ->
                    creator = userDoc.data
                    if (userId != null) {
                        db.collection("users").document(userId).get().addOnSuccessListener { currentUserDoc ->
                            val following = (currentUserDoc.get("following") as? List<*>)?.map { it.toString() } ?: emptyList()
                            isFollowing = following.contains(creatorId)
                            isLoading = false
                        }
                    } else {
                        isLoading = false
                    }
                }
            } else {
                isLoading = false
            }
        }
    }

    fun toggleLike(postId: String) {
        if (userId == null) return
        val creatorUid = creator?.get("uid") as? String ?: ""
        val postRef = db.collection("posts").document(postId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likedBy = (snapshot.get("likedBy") as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (likedBy.contains(userId)) {
                likedBy.remove(userId)

            } else {
                likedBy.add(userId)
                sendNotificationWithType(
                    fromUserId = userId.toString(),
                    toUserId = creatorUid.toString(),
                    type = "like",
                    postId = postId
                )
            }
            transaction.update(postRef, "likedBy", likedBy)
            transaction.update(postRef, "likes", likedBy.size)
        }.addOnSuccessListener {
            isLiked = !isLiked
            likeCount += if (isLiked) 1 else -1

        }
    }

    fun toggleSave(postId: String) {
        if (userId == null) return
        val postRef = db.collection("posts").document(postId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val savedBy = (snapshot.get("savedBy") as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (savedBy.contains(userId)) {
                savedBy.remove(userId)
            } else {
                savedBy.add(userId)
            }
            transaction.update(postRef, "savedBy", savedBy)

        }.addOnSuccessListener {
            isSaved = !isSaved
            saveCount += if (isSaved) 1 else -1
        }
    }

    fun toggleFollow() {
        if (userId == null || creator == null) return
        val creatorId = creator?.get("uid") as? String ?: return
        val currentUserRef = db.collection("users").document(userId)
        val creatorRef = db.collection("users").document(creatorId)
        db.runBatch { batch ->
            if (isFollowing) {
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(creatorId))
                batch.update(creatorRef, "followers", FieldValue.arrayRemove(userId))
            } else {
                batch.update(currentUserRef, "following", FieldValue.arrayUnion(creatorId))
                batch.update(creatorRef, "followers", FieldValue.arrayUnion(userId))
                sendNotificationWithType(
                    fromUserId = userId,
                    toUserId = creatorId,
                    type = "follow"
                )

            }
        }.addOnSuccessListener {
            isFollowing = !isFollowing
        }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit) {
        db.collection("posts").document(postId).delete().addOnSuccessListener {
            onSuccess()
        }
    }

    fun showPagerIndicatorWithDelay() {
        showPagerIndicator = true
        pagerIndicatorJob?.cancel()
        pagerIndicatorJob = viewModelScope.launch(Dispatchers.Main) {
            delay(1500)
            showPagerIndicator = false
        }
    }

    fun downloadPhoto(context: Context, url: String) {
        val request = DownloadManager.Request(url.toUri())
            .setTitle("ecoai_image.jpg")
            .setDescription("Downloading image...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ecoai_image.jpg")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }

    fun getMediaList(): List<Map<String, String>> {
        return (post?.get("media") as? List<*>)?.mapNotNull {
            it as? Map<*, *>
        }?.map { map ->
            map.mapKeys { it.key.toString() }.mapValues { it.value?.toString() ?: "" }
        } ?: emptyList()
    }

    fun getCreatedDateString(): String {
        val createdAt = post?.get("createdAt")
        return (createdAt as? com.google.firebase.Timestamp)?.let {
            val date = Date(it.seconds * 1000)
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
        } ?: ""
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            if (userId == null) return@launch
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
            val commentDoc = commentRef.get().await()
            if (commentDoc.get("userId") == userId) {
                commentRef.delete().await()
            }
        }
    }
} 