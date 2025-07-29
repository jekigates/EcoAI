package com.bluejack242.ecoai.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.bluejack242.ecoai.model.WasteHistoryItem
import com.bluejack242.ecoai.model.WasteItem
@Composable
fun RecentlyUploadedItemCard(item: WasteHistoryItem, onClick: (String) -> Unit) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick(item.id) }
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = item.imageRes,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            ) {
                Text(item.name, style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(4.dp))
                Text("${item.co2e} gram CO2e", style = MaterialTheme.typography.body2)
            }
        }
    }
}
