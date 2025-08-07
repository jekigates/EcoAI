package edu.bluejack24_2.ecoai.model

import com.google.firebase.auth.FirebaseAuth

/**
 * Unified UI model for displaying a post in grids/lists.
 */
data class PostUiModel(
    val postId: String,
    val imageUrl: String,
    val title: String,
    val fullName: String,
    val profilePictureUrl: String,
    val likes: Int,
    val liked: Boolean,
    val saves: Int,
    val saved: Boolean,
    val commentsCount: Int
)

fun mapPostToUiModel(postId: String, post: Map<String, Any>, fullNameFallback: String = "", profilePictureUrlFallback: String = "", commentsCount: Int = 0): PostUiModel {
    @Suppress("UNCHECKED_CAST")
    val mediaList = post["media"] as? List<Map<String, Any>> ?: emptyList()
    val imageUrl = mediaList.firstOrNull()?.get("url") as? String ?: ""
    val title = post["headline"] as? String ?: ""
    val fullName = (post["fullName"] as? String).orEmpty().ifBlank { fullNameFallback }
    val profilePictureUrl = (post["profilePictureUrl"] as? String).orEmpty().ifBlank { profilePictureUrlFallback }
    val likes = (post["likes"] as? Long)?.toInt() ?: 0
    val likedBy = post["likedBy"] as? List<*> ?: emptyList<Any>()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val liked = currentUserId != null && likedBy.contains(currentUserId)
    val savedBy = post["savedBy"] as? List<*> ?: emptyList<Any>()
    val saved = currentUserId != null && savedBy.contains(currentUserId)
    val saves = savedBy.size
    return PostUiModel(
        postId = postId,
        imageUrl = imageUrl,
        title = title,
        fullName = fullName,
        profilePictureUrl = profilePictureUrl,
        likes = likes,
        liked = liked,
        saves = saves,
        saved = saved,
        commentsCount = commentsCount
    )
}
