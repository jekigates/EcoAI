package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bluejack242.ecoai.model.WasteHistoryItem


@Composable
fun HistoryScreen(
//    historyList: List<WasteHistoryItem>,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "History",
            style = MaterialTheme.typography.h3,
            fontWeight = FontWeight.Bold
        )

//        if (historyList.isEmpty()) {
//            Spacer(Modifier.height(32.dp))
//            Text("No history yet", color = Color.Gray)
//        } else {
//            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                items(historyList) { item ->
//                    HistoryItemRow(item, onItemClick)
//                }
//            }
//        }
    }
}
@Composable
fun HistoryItemRow(item: WasteHistoryItem, onItemClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onItemClick(item.id) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageRes,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text("${item.co2e} gram CO2e", style = MaterialTheme.typography.body2)
            }
            Text(
                item.date.toString(),
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
