package edu.bluejack24_2.ecoai.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.bluejack24_2.ecoai.utils.CloudinaryService
import edu.bluejack24_2.ecoai.utils.LanguageManager
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

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var bioError by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) profilePictureUri = uri
    }

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

    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val buttonColor = MaterialTheme.colorScheme.primary
    val isLightTheme = backgroundColor.luminance() > 0.5f
    val profilePictureBg = if (isLightTheme) MaterialTheme.colorScheme.surfaceVariant else onSurfaceVariantColor.copy(alpha = 0.2f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("edit_profile"),
            navController = navController
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            // Profile Picture
            Box(
                modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally)
                    .clip(CircleShape).background(profilePictureBg)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = profilePictureUri ?: profilePictureUrl, contentDescription = null)
            }

            Spacer(Modifier.height(24.dp))

            // Full name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(LanguageManager.getString("full_name"), color = onSurfaceVariantColor) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                isError = fullNameError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    focusedBorderColor = onSurfaceVariantColor,
                    unfocusedBorderColor = onSurfaceVariantColor,
                    errorBorderColor = errorColor,
                    errorLabelColor = errorColor
                )
            )
            if (fullNameError != null) {
                Text(fullNameError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            }

            Spacer(Modifier.height(12.dp))
            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(LanguageManager.getString("username"), color = onSurfaceVariantColor) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                isError = usernameError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    focusedBorderColor = onSurfaceVariantColor,
                    unfocusedBorderColor = onSurfaceVariantColor,
                    errorBorderColor = errorColor,
                    errorLabelColor = errorColor
                )
            )
            if (usernameError != null) {
                Text(usernameError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            }

            Spacer(Modifier.height(12.dp))
            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(LanguageManager.getString("bio"), color = onSurfaceVariantColor) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                isError = bioError != null,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    focusedBorderColor = onSurfaceVariantColor,
                    unfocusedBorderColor = onSurfaceVariantColor,
                    errorBorderColor = errorColor,
                    errorLabelColor = errorColor
                )
            )
            if (bioError != null) {
                Text(bioError!!, color = errorColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    fullNameError = null
                    usernameError = null
                    bioError = null

                    var valid = true
                    if (fullName.length < 4) {
                        fullNameError = LanguageManager.getString("name_required")
                        valid = false
                    }
                    if (bio.trim().split("\\s+".toRegex()).size < 3) {
                        bioError = LanguageManager.getString("bio_required")
                        valid = false
                    }

                    if (!valid || user == null) return@Button

                    isSaving = true
                    db.collection("users").whereEqualTo("username", username).get()
                        .addOnSuccessListener { result ->
                            val usernameTaken = result.any { it.id != user.uid }
                            if (usernameTaken) {
                                usernameError = LanguageManager.getString("username_taken")
                                isSaving = false
                            } else {
                                coroutineScope.launch {
                                    var uploadedUrl: String? = profilePictureUrl
                                    if (profilePictureUri != null) {
                                        val uploadResult = cloudinaryService.uploadProfileImage(context, profilePictureUri!!)
                                        uploadResult.onSuccess { url -> uploadedUrl = url }
                                        uploadResult.onFailure {
                                            Toast.makeText(context, LanguageManager.getString("upload_failed") + it.message, Toast.LENGTH_SHORT).show()
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
                                            Toast.makeText(context, LanguageManager.getString("profile_updated"), Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, LanguageManager.getString("failed_update") + it.message, Toast.LENGTH_SHORT).show()
                                        }
                                    isSaving = false
                                }
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                enabled = !isSaving
            ) {
                Text(
                    if (isSaving) LanguageManager.getString("saving") else LanguageManager.getString("save"),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
