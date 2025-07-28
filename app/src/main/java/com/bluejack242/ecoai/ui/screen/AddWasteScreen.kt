package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun AddWasteScreen(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onCameraClick) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera", tint = Color(0xFF388E3C))
        }
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onGalleryClick) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery", tint = Color(0xFF388E3C))
        }
    }
}

