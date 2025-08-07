package edu.bluejack24_2.ecoai.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ClassificationResultScreen(
    imageUri: Uri,
    itemName: String,
    carbon: Int,
    onConfirm: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = "Item", modifier = Modifier.fillMaxWidth().height(200.dp))
        Text(itemName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Carbon Footprint: $carbon gram CO2e")
        Spacer(Modifier.weight(1f))
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}
