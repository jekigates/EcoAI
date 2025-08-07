package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import edu.bluejack24_2.ecoai.model.Notification
import edu.bluejack24_2.ecoai.model.User
import edu.bluejack24_2.ecoai.ui.component.BottomNavigationBar
import edu.bluejack24_2.ecoai.ui.component.NotificationCard
import edu.bluejack24_2.ecoai.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NotificationScreen(
    navController: NavController,
    currentRoute: String,
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    val users = remember { mutableStateMapOf<String, User>() }
    var isLoading by remember { mutableStateOf(true) }

    val realtimeRef = FirebaseDatabase.getInstance()
        .getReference("notifications")
        .child(currentUserId ?: "")

    LaunchedEffect(currentUserId) {
        if (currentUserId == null) {
            isLoading = false
            notifications = emptyList()
            return@LaunchedEffect
        }

        realtimeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading = false
                val temp = snapshot.children.mapNotNull { snap ->
                    val map = snap.value as? Map<String, Any> ?: return@mapNotNull null
                    val createdAtMap = map["createdAt"] as? Map<String, Any>
                    val timestamp = if (createdAtMap != null) {
                        val seconds = (createdAtMap["seconds"] as? Number)?.toLong() ?: 0L
                        val nanos = (createdAtMap["nanoseconds"] as? Number)?.toInt() ?: 0
                        com.google.firebase.Timestamp(seconds, nanos)
                    } else {
                        com.google.firebase.Timestamp.now()
                    }
                    Notification(
                        fromUserId = map["fromUserId"] as? String ?: "",
                        toUserId = map["toUserId"] as? String ?: "",
                        postId = map["postId"] as? String ?: "",
                        type = map["type"] as? String ?: "",
                        createdAt = timestamp
                    )
                }

                notifications = temp.sortedByDescending { it.createdAt.toDate().time }

                temp.forEach { notif ->
                    if (!users.containsKey(notif.fromUserId)) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(notif.fromUserId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                userDoc.toObject(User::class.java)?.let { users[notif.fromUserId] = it }
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = LanguageManager.getString("notification"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        Text(
                            text = LanguageManager.getString("no_notifications"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        // Header
                        item {
                            Text(
                                text = LanguageManager.getString("notification"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }

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
                                        Text("Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
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