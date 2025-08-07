package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import edu.bluejack24_2.ecoai.ui.component.UserRow
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar

@Composable
fun UserListScreen(
    title: String,
    userIds: List<String>,
    navController: NavHostController
) {
    val db = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userIds) {
        val fetchedUsers = mutableListOf<Map<String, Any>>()
        userIds.forEach { id ->
            val doc = db.collection("users").document(id).get().await()
            doc.data?.let { fetchedUsers.add(it + mapOf("id" to id)) }
        }
        users = fetchedUsers
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackHeaderBar(
            title = title,
            navController = navController
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(users) { user ->
                    UserRow(
                        name = user["fullName"] as? String ?: "No Name",
                        username = user["username"] as? String ?: "",
                        profilePic = user["profilePictureUrl"] as? String ?: ""
                    ) {
                        val id = user["id"] as? String ?: ""
                        navController.navigate("user_profile/$id")
                    }
                }
            }
        }
    }
}
