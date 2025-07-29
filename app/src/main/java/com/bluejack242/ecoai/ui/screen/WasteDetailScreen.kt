package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bluejack242.ecoai.model.WasteItem

@Composable
fun WasteDetailScreen(
    item: WasteItem,
    onDone: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AsyncImage(
            model = item.imageRes,
            contentDescription = item.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Text("Carbon Footprint: ${item.co2e} g CO2e", fontSize = 16.sp)

        Text("Disposal Methods: Recycle, Compost", fontSize = 16.sp)

        Spacer(Modifier.weight(1f))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(contentColor = Color(0xFF388E3C))) {
            Text("Done")
        }
    }
}