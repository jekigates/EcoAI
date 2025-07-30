package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bluejack242.ecoai.model.WasteHistoryItem
import com.bluejack242.ecoai.viewmodel.ProgressViewModel

@Composable
fun WasteDetailScreen(
    viewModel: ProgressViewModel,
    itemId: String,
    onDone: () -> Unit
) {
    val itemState = produceState<WasteHistoryItem?>(initialValue = null, itemId) {
        value = viewModel.getWasteItemById(itemId)
    }

    itemState.value?.let { item ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AsyncImage(
                model = item.imageRes,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(all = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                backgroundColor = Color(0xFFF7F7F7)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🌍 Carbon Footprint",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "${item.co2e} g CO2e",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "♻️ Disposal Method",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = item.disposalMethod,
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
            ) {
                Text("Done", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    } ?: run {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
