package com.bluejack242.ecoai.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bluejack242.ecoai.utils.CloudinaryService
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val cloudinaryService = remember { CloudinaryService() }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    var isProfileLoaded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            profilePictureUri = uri
        }
    }

    // Fetch user profile from Firestore on first composition
    LaunchedEffect(user?.uid) {
        if (user != null && !isProfileLoaded) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                fullName = doc.getString("fullName") ?: ""
                username = doc.getString("username") ?: ""
                bio = doc.getString("bio") ?: ""
                profilePictureUrl = doc.getString("profilePictureUrl")
                isProfileLoaded = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar with back button and title
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edit Profile",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black
            )
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
        Spacer(Modifier.height(24.dp))
        // Profile picture
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            when {
                profilePictureUri != null -> {
                    AsyncImage(
                        model = profilePictureUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(100.dp)
                    )
                    // Pencil overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                !profilePictureUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(100.dp)
                    )
                    // Pencil overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                else -> {
                    // Gray circle with pencil
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = { if (it.length <= 30) fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Text(
            text = "${fullName.length}/30",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            fontSize = 12.sp,
            color = if (fullName.length == 30) Color.Red else Color.Gray
        )
        Spacer(Modifier.height(12.dp))
        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(12.dp))
        // Bio
        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 80) bio = it },
            label = { Text("Bio") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            maxLines = 3
        )
        Text(
            text = "${bio.length}/80",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            fontSize = 12.sp,
            color = if (bio.length == 80) Color.Red else Color.Gray
        )
        Spacer(Modifier.height(24.dp))
        // Save button
        Button(
            onClick = {
                if (user == null) return@Button
                isSaving = true
                coroutineScope.launch {
                    var uploadedUrl: String? = profilePictureUrl
                    if (profilePictureUri != null) {
                        val result = cloudinaryService.uploadProfileImage(context, profilePictureUri!!)
                        result.onSuccess { url ->
                            uploadedUrl = url
                        }
                        result.onFailure {
                            Toast.makeText(context, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                            isSaving = false
                            return@launch
                        }
                    }
                    val userMap = hashMapOf(
                        "fullName" to fullName,
                        "username" to username,
                        "bio" to bio,
                        "profilePictureUrl" to (uploadedUrl ?: "")
                    )
                    db.collection("users").document(user.uid)
                        .update(userMap as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    isSaving = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Saving..." else "Save", color = Color.White)
        }
    }
} 