package com.bluejack242.ecoai.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.bluejack242.ecoai.viewmodel.WasteViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import com.bluejack242.ecoai.utils.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AddWasteScreen(
    viewModel: WasteViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val handleImageSelected: (Uri) -> Unit = { uri ->
        isLoading = true
        coroutineScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream)
                }

                viewModel.addWasteItemWithImage(
                    context = context,
                    imageBitmap = bitmap,
                    imageUri = uri,
                    uploadedBy = userId,
                    onSuccess = {
                        isLoading = false
                        navController.navigateUp()
                    }
                )
            } catch (e: Exception) {
                Log.e("AddWasteScreen", "Error addWasteItem: ${e.message}", e)
                errorMessage = "${LanguageManager.getString("error_occurred")}: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }


    // camera launcher
    var tempUri: Uri? by remember { mutableStateOf(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            handleImageSelected(tempUri!!)
        }
    }

    // gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleImageSelected(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempUri?.let { cameraLauncher.launch(it) }
        } else {
            errorMessage = LanguageManager.getString("camera_permission_denied")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LanguageManager.getString("add_waste_title")) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = LanguageManager.getString("back")
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val imageFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
                            tempUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                imageFile
                            )
                            tempUri?.let { cameraLauncher.launch(it) }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageManager.getString("open_camera"))
                }


                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageManager.getString("open_gallery"))
                }

                OutlinedButton(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AccessTimeFilled, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageManager.getString("open_history"))
                }
            }
        }
    }
}
