package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.bluejack242.ecoai.model.Notification
import com.bluejack242.ecoai.model.User
import com.bluejack242.ecoai.ui.component.BottomNavigationBar
import com.bluejack242.ecoai.ui.component.NotificationCard
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

@Composable
fun NotificationScreen(
    navController: NavController,
    currentRoute: String,
) {
    val db = Firebase.firestore
    val notifications = remember { mutableStateListOf<Notification>() }
    val users = remember { mutableStateMapOf<String, User>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUserId) {
        db.collection("notifications")
            .whereEqualTo("toUserId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    notifications.clear()
                    it.documents.forEach { doc ->
                        val notif = doc.toObject(Notification::class.java)
                        notif?.let { n ->
                            notifications.add(n)

                            val fromId = n.fromUserId
                            if (!users.containsKey(fromId)) {
                                db.collection("users").document(fromId)
                                    .get().addOnSuccessListener { userDoc ->
                                        val user = userDoc.toObject(User::class.java)
                                        user?.let { u -> users[fromId] = u }
                                    }
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
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada notifikasi.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(notifications) { notif ->
                    users[notif.fromUserId]?.let { fromUser ->
                        NotificationCard(
                            notification = notif,
                            fromUser = fromUser,
                            onPostClick = { postId -> navController.navigate("postDetail/$postId") },
                            onUserClick = { userId -> navController.navigate("profile/$userId") }
                        )
                    }
                }
            }
        }
    }
}
