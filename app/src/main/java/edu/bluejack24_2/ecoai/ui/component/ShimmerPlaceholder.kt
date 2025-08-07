package edu.bluejack24_2.ecoai.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    baseColor: Color = Color.LightGray,
    highlightColor: Color = Color.White.copy(alpha = 0.6f),
    shape: androidx.compose.foundation.shape.CornerBasedShape? = null
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmerAnim"
    )
    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = androidx.compose.ui.geometry.Offset(translateAnim - 1000f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, 1000f)
    )
    Box(
        modifier = if (shape != null) modifier.clip(shape).background(brush) else modifier.background(brush)
    )
}
