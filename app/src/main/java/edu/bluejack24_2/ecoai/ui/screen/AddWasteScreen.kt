package edu.bluejack24_2.ecoai.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
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
import edu.bluejack24_2.ecoai.viewmodel.WasteViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import edu.bluejack24_2.ecoai.utils.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                preview.setSurfaceProvider(previewView.surfaceProvider)
                try {
                    cameraProvider.unbindAll()
                    if (imageCapture != null) {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } else {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )
                    }
                } catch (exc: Exception) {
                    android.widget.Toast.makeText(
                        ctx,
                        "Failed to start camera: ${exc.localizedMessage}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    Log.e("CameraPreview", "Failed to bind camera use cases", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

@Composable
fun AddWasteScreen(
    viewModel: WasteViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showCameraPreview by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val handleImageSelected: (Uri) -> Unit = { uri ->
        isLoading = true
        showCameraPreview = false
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


    // camera logic state
    // CameraX in-app capture state
    val imageCapture = remember { ImageCapture.Builder().build() }

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
            showCameraPreview = true
        } else {
            showErrorDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("add_waste_title"),
            navController = navController
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (showCameraPreview) {
                // CameraX preview with Close and Capture icon buttons
                CameraPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(MaterialTheme.shapes.medium),
                    imageCapture = imageCapture
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { showCameraPreview = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = LanguageManager.getString("close_camera"))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            // In-app CameraX capture
                            val imageFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            imageFile
                                        )
                                        handleImageSelected(uri)
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        errorMessage = "${LanguageManager.getString("error_occurred")}: ${exception.localizedMessage}"
                                        showCameraPreview = false
                                    }
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = LanguageManager.getString("capture"))
                    }
                }
            } else {
                // Initial state: show Open Camera and Open Gallery
                if (showErrorDialog) {
                    edu.bluejack24_2.ecoai.ui.component.CustomDialog(
                        title = LanguageManager.getString("camera_permission_error_title"),
                        message = LanguageManager.getString("camera_permission_error_message"),
                        confirmText = LanguageManager.getString("ok"),
                        onConfirm = { showErrorDialog = false },
                        onDismiss = { showErrorDialog = false },
                        dialogType = edu.bluejack24_2.ecoai.ui.component.DialogType.Error
                    )
                }
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            showCameraPreview = true
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageManager.getString("open_camera"))
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LanguageManager.getString("open_gallery"))
                }
            }
        }
    }
}
