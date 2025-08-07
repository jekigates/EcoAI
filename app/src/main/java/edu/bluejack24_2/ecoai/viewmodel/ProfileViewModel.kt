package edu.bluejack24_2.ecoai.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import edu.bluejack24_2.ecoai.utils.sendNotificationWithType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    fun resetPostsForTab(tab: Int) {
        isLoadingPosts = true
        when (tab) {
            0 -> ownPosts = emptyList()
            1 -> savedPosts = emptyList()
            2 -> likedPosts = emptyList()
        }
    }
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    var isProfileLoaded by mutableStateOf(false)
    var username by mutableStateOf("")
    var bio by mutableStateOf("")
    var profilePictureUrl by mutableStateOf<String?>(null)
    var fullName by mutableStateOf("")
    var followers by mutableStateOf<List<String>>(emptyList())
    var following by mutableStateOf<List<String>>(emptyList())
    var likes by mutableIntStateOf(0)

    var ownPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
    var likedPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
    var savedPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
    var isLoadingPosts by mutableStateOf(false)

    fun fetchProfile() {
        isLoadingPosts = true
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                fullName = doc.getString("fullName") ?: ""
                username = doc.getString("username") ?: ""
                bio = doc.getString("bio") ?: ""
                profilePictureUrl = doc.getString("profilePictureUrl")
                isProfileLoaded = true
                @Suppress("UNCHECKED_CAST")
                followers = (doc.get("followers") as? List<String>) ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                following = (doc.get("following") as? List<String>) ?: emptyList()
                db.collection("posts").whereEqualTo("userId", userId).get().addOnSuccessListener { posts ->
                    likes = posts.documents.sumOf { (it.get("likes") as? Long)?.toInt() ?: 0 }
                    isLoadingPosts = false
                }
            }
        }
    }

    fun fetchPostsForTab(tab: Int) {
        isLoadingPosts = true
        when (tab) {
            0 -> {
                db.collection("posts").whereEqualTo("userId", userId).get().addOnSuccessListener { result ->
                    @Suppress("UNCHECKED_CAST")
                    ownPosts = result.documents.mapNotNull { doc -> doc.id to (doc.data as? Map<String, Any>) }.filter { it.second != null } as List<Pair<String, Map<String, Any>>>
                    isLoadingPosts = false
                }
            }
            1 -> {
                db.collection("posts").whereArrayContains("savedBy", userId ?: "").get().addOnSuccessListener { result ->
                    @Suppress("UNCHECKED_CAST")
                    savedPosts = result.documents.mapNotNull { doc -> doc.id to (doc.data as? Map<String, Any>) }.filter { it.second != null } as List<Pair<String, Map<String, Any>>>
                    isLoadingPosts = false
                }
            }
            2 -> {
                db.collection("posts").whereArrayContains("likedBy", userId ?: "").get().addOnSuccessListener { result ->
                    @Suppress("UNCHECKED_CAST")
                    likedPosts = result.documents.mapNotNull { doc -> doc.id to (doc.data as? Map<String, Any>) }.filter { it.second != null } as List<Pair<String, Map<String, Any>>>
                    isLoadingPosts = false
                }
            }
        }
    }
}

class UserProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    var userId: String? by mutableStateOf(null)

    private var isProfileLoaded by mutableStateOf(false)
    var username by mutableStateOf("")
    var bio by mutableStateOf("")
    var profilePictureUrl by mutableStateOf<String?>(null)
    var fullName by mutableStateOf("")
    var followers by mutableStateOf<List<String>>(emptyList())
    var following by mutableStateOf<List<String>>(emptyList())
    var likes by mutableIntStateOf(0)
    var isFollowing by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var userPosts by mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())

    fun setUserIdAndFetch(userId: String) {
        this.userId = userId
        fetchProfile()
        fetchUserPosts(userId)
    }

    private fun fetchProfile() {
        val uid = userId ?: return
        isLoading = true
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            fullName = doc.getString("fullName") ?: ""
            username = doc.getString("username") ?: ""
            bio = doc.getString("bio") ?: ""
            profilePictureUrl = doc.getString("profilePictureUrl")
            @Suppress("UNCHECKED_CAST")
            followers = (doc.get("followers") as? List<String>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            following = (doc.get("following") as? List<String>) ?: emptyList()
            isProfileLoaded = true
            isFollowing = currentUser?.uid?.let { followers.contains(it) } == true
            db.collection("posts").whereEqualTo("userId", uid).get().addOnSuccessListener { posts ->
                likes = posts.documents.sumOf { (it.get("likes") as? Long)?.toInt() ?: 0 }
                isLoading = false
            }
        }.addOnFailureListener {
            isLoading = false
        }
    }

    private fun fetchUserPosts(userId: String) {
        db.collection("posts").whereEqualTo("userId", userId).get().addOnSuccessListener { result ->
            @Suppress("UNCHECKED_CAST")
            userPosts = result.documents.mapNotNull { doc -> doc.id to (doc.data as? Map<String, Any>) }.filter { it.second != null } as List<Pair<String, Map<String, Any>>>
        }
    }

    fun toggleFollow() {
        val uid = userId ?: return
        val myUid = currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)
        val myRef = db.collection("users").document(myUid)
        db.runBatch { batch ->
            if (isFollowing) {
                batch.update(userRef, "followers", com.google.firebase.firestore.FieldValue.arrayRemove(myUid))
                batch.update(myRef, "following", com.google.firebase.firestore.FieldValue.arrayRemove(uid))
            } else {
                batch.update(userRef, "followers", com.google.firebase.firestore.FieldValue.arrayUnion(myUid))
                batch.update(myRef, "following", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                sendNotificationWithType(
                    fromUserId = myUid,
                    toUserId = uid,
                    type = "follow"
                )
            }
        }.addOnSuccessListener {
            isFollowing = !isFollowing
            followers = if (isFollowing) followers + myUid else followers - myUid
        }
    }
} 