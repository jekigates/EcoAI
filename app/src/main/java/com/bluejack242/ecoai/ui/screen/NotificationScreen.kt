package com.bluejack242.ecoai.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.bluejack242.ecoai.model.Notification
import com.bluejack242.ecoai.model.User
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.bluejack242.ecoai.ui.component.NotificationCard
import com.bluejack242.ecoai.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

@Composable
fun NotificationScreen(
    navController: NavController,
    currentRoute: String,
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    val users = remember { mutableStateMapOf<String, User>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId == null) {
            isLoading = false
            notifications = emptyList()
            return@LaunchedEffect
        }

        db.collection("notifications")
            .whereEqualTo("toUserId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                isLoading = false
                val temp = snapshot?.documents?.mapNotNull { it.toObject(Notification::class.java) } ?: emptyList()
                notifications = temp

                // Fetch related users
                temp.forEach { notif ->
                    val fromId = notif.fromUserId
                    if (!users.containsKey(fromId)) {
                        Log.d("NotificationScreen", "Fetching user: $fromId")
                        db.collection("users").document(fromId)
                            .get().addOnSuccessListener { userDoc ->
                                userDoc.toObject(User::class.java)?.let {
                                    users[fromId] = it
                                }
                            }
                    }
                }
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController as NavHostController,
                currentRoute = currentRoute
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                notifications.isEmpty() -> {
                    Text(
                        text = LanguageManager.getString("no_notifications"),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(
                            notifications,
                            key = { it.fromUserId + it.createdAt.toDate().time }
                        ) { notif ->
                            val fromUser = users[notif.fromUserId]
                            if (fromUser != null) {
                                NotificationCard(
                                    notification = notif,
                                    fromUser = fromUser,
                                    onPostClick = { postId ->
                                        navController.navigate("post_detail/$postId")
                                    },
                                    onUserClick = { userId ->
                                        navController.navigate("user_profile/$userId")
                                    }
                                )
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text("Memuat data pengguna...", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun fetchUserById(userId: String): User? {
    return try {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()
        snapshot.toObject(User::class.java)
    } catch (e: Exception) {
        null
    }
}