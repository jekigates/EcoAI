package edu.bluejack24_2.ecoai.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack24_2.ecoai.model.MediaItem
import edu.bluejack24_2.ecoai.model.MediaType
import edu.bluejack24_2.ecoai.model.PostRequest
import edu.bluejack24_2.ecoai.utils.CloudinaryService
import edu.bluejack24_2.ecoai.utils.ValidationUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreatePostViewModel : ViewModel() {
    private val cloudinaryService = CloudinaryService()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _postRequest = MutableStateFlow(PostRequest())
    val postRequest: StateFlow<PostRequest> = _postRequest

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    fun updateHeadline(text: String) {
        _postRequest.value = _postRequest.value.copy(headline = text)
    }

    fun updateCaption(text: String) {
        _postRequest.value = _postRequest.value.copy(caption = text)
    }

    fun uploadMedia(context: Context, uri: Uri, isVideo: Boolean, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val result = cloudinaryService.uploadMedia(context, uri, isVideo)
                result.onSuccess { url ->
                    Toast.makeText(context, "Uploaded URL: $url", Toast.LENGTH_SHORT).show()
                    val currentPost = _postRequest.value
                    val updatedList = currentPost.mediaList + MediaItem(url, if (isVideo) MediaType.VIDEO else MediaType.IMAGE)
                    _postRequest.value = currentPost.copy(mediaList = updatedList)
                }
                result.onFailure {
                    onError("Upload failed: ${it.message}")
                }
            } catch (e: Exception) {
                onError("Upload failed: ${e.message}")
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun removeMediaAt(index: Int) {
        val currentPost = _postRequest.value
        if (index in currentPost.mediaList.indices) {
            val updatedList = currentPost.mediaList.toMutableList()
            updatedList.removeAt(index)
            _postRequest.value = currentPost.copy(mediaList = updatedList)
        }
    }

    fun createPost(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val post = _postRequest.value
        val validationError = ValidationUtil.validatePost(post.headline, post.caption, post.mediaList.size)
        if (validationError != null) {
            onError(validationError)
            return
        }
        _isPosting.value = true
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                val data = mapOf(
                    "userId" to userId,
                    "headline" to post.headline,
                    "caption" to post.caption,
                    "media" to post.mediaList.map { mediaItem ->
                        mapOf("url" to mediaItem.url, "type" to mediaItem.type.name)
                    },
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                firestore.collection("posts").add(data)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError("Failed to save post: ${e.message}") }
            } catch (e: Exception) {
                onError("Error creating post: ${e.message}")
            } finally {
                _isPosting.value = false
            }
        }
    }
}