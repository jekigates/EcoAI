package edu.bluejack24_2.ecoai.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import edu.bluejack24_2.ecoai.model.MediaType
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.viewmodel.CreatePostViewModel

import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

import coil.compose.AsyncImage
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CreatePostScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: CreatePostViewModel = viewModel()
    val post by viewModel.postRequest.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isPosting by viewModel.isPosting.collectAsState()

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { pickedUri ->
            if (post.mediaList.count { it.type == MediaType.IMAGE } < 10) {
                viewModel.uploadMedia(context, pickedUri, false) { _ ->
                    Toast.makeText(context, LanguageManager.getString("error_uploading_image"), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, LanguageManager.getString("max_images_reached"), Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(context, LanguageManager.getString("no_image_selected"), Toast.LENGTH_SHORT).show()
        }
    }
    val imageCount = post.mediaList.count { it.type == MediaType.IMAGE }

    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val buttonColor = MaterialTheme.colorScheme.primary
    val buttonTextColor = MaterialTheme.colorScheme.onPrimary
    val cardBgColor = if (backgroundColor.luminance() > 0.5f) MaterialTheme.colorScheme.surfaceVariant else onSurfaceVariantColor.copy(alpha = 0.2f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("create_new_post"),
            navController = navController
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Media Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (post.mediaList.isEmpty()) cardBgColor else Color.Transparent)
            ) {
                if (post.mediaList.isEmpty()) {
                    Text(
                        LanguageManager.getString("no_media_yet"),
                        color = onSurfaceVariantColor,
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
                                    contentDescription = LanguageManager.getString("remove_image"),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        activeColor = onSurfaceColor,
                        inactiveColor = onSurfaceVariantColor
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
                label = { Text(LanguageManager.getString("headline_optional"), color = onSurfaceVariantColor) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Text(headlineSupportingText, color = onSurfaceVariantColor)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    focusedBorderColor = onSurfaceVariantColor,
                    unfocusedBorderColor = onSurfaceVariantColor,
                    cursorColor = onSurfaceColor
                )
            )
            Spacer(Modifier.height(8.dp))

            // Caption and Tags
            OutlinedTextField(
                value = post.caption,
                onValueChange = { value -> viewModel.updateCaption(value) },
                label = { Text(LanguageManager.getString("caption_and_tags_optional"), color = onSurfaceVariantColor) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    focusedBorderColor = onSurfaceVariantColor,
                    unfocusedBorderColor = onSurfaceVariantColor,
                    cursorColor = onSurfaceColor
                )
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { imageLauncher.launch("image/*") },
                enabled = !isUploading && imageCount < 10,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(
                    color = buttonTextColor,
                    text = if (isUploading)
                        LanguageManager.getString("uploading")
                    else
                        "${LanguageManager.getString("add_image")} (${imageCount}/10)"
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.createPost(
                        onSuccess = {
                            Toast.makeText(context, LanguageManager.getString("post_created"), Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onError = { errMsg ->
                            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = !isPosting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(LanguageManager.getString("post_button"), color = buttonTextColor)
            }
        }
    }
}