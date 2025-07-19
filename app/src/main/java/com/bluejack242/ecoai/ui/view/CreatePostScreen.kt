package com.bluejack242.ecoai.ui.view

// AndroidX Compose
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bluejack242.ecoai.model.MediaItem
import com.bluejack242.ecoai.model.MediaType
import com.bluejack242.ecoai.ui.viewmodel.CreatePostViewModel

// Accompanist Pager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

// Coil
import coil.compose.AsyncImage

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CreatePostScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: CreatePostViewModel = viewModel()
    val post by viewModel.postRequest.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isPosting by viewModel.isPosting.collectAsState()

    // Launcher for picking media
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (post.mediaList.count { it.type == MediaType.IMAGE } < 10) {
                viewModel.uploadMedia(context, it, false) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
    val imageCount = post.mediaList.count { it.type == MediaType.IMAGE }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Back")
            }
            Text("Create New Post", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp)) // Placeholder for alignment
        }

        // Media Carousel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (post.mediaList.isEmpty()) Color.LightGray else Color.Transparent)
        ) {
            if (post.mediaList.isEmpty()) {
                Text(
                    "No media yet",
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val pagerState = rememberPagerState()
                HorizontalPager(
                    count = post.mediaList.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    val media = post.mediaList[index]
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = media.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.removeMediaAt(index) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White
                            )
                        }
                    }
                }
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    activeColor = Color.Black,
                    inactiveColor = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Headline
        val headlineSupportingText = "${post.headline.length}/50"
        OutlinedTextField(
            value = post.headline,
            onValueChange = {
                if (it.length <= 50) viewModel.updateHeadline(it)
            },
            label = { Text("Headline (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            // Use trailingIcon for char count, since supportingText is not available in Material 2
            trailingIcon = {
                Text(headlineSupportingText)
            }
        )
        Spacer(Modifier.height(8.dp))

        // Caption and Tags
        OutlinedTextField(
            value = post.caption,
            onValueChange = viewModel::updateCaption,
            label = { Text("Caption and tags (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )
        Spacer(Modifier.height(16.dp))

        // Add Image Button only
        Button(
            onClick = { imageLauncher.launch("image/*") },
            enabled = !isUploading && imageCount < 10,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50))
        ) {
            Text(color = Color.White, text = if (isUploading) "Uploading..." else "Add Image (${imageCount}/10)")
        }

        Spacer(Modifier.height(16.dp))

        // Update Post Button enable logic: must have at least 1 image
        Button(
            onClick = {
                viewModel.createPost(
                    onSuccess = {
                        Toast.makeText(context, "Post created!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isPosting && post.mediaList.isNotEmpty() && post.mediaList.size <= 10,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
        ) {
            Text("Post", color = Color.White)
        }
    }
}