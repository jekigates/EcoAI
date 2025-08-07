package edu.bluejack24_2.ecoai.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun EcoAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: androidx.compose.foundation.shape.CornerBasedShape = RoundedCornerShape(4.dp),
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    errorColor: Color = MaterialTheme.colorScheme.errorContainer
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentScale = contentScale
    )
    val state = painter.state
    Box(modifier = modifier.clip(shape)) {
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale
        )
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                ShimmerPlaceholder(
                    modifier = Modifier.matchParentSize(),
                    baseColor = placeholderColor,
                    shape = shape
                )
            }
            is AsyncImagePainter.State.Error -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(errorColor)
                )
            }
            else -> {}
        }
    }
}
