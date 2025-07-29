package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluejack242.ecoai.model.WasteItem
import com.bluejack242.ecoai.ui.component.WasteItemRow

@Composable
fun WasteDatabaseScreen(
    wasteItems: List<WasteItem>,
    onItemClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    searchQuery: String
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Waste Database", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn {
//            items(wasteItems) { item ->
//                WasteItemRow(item) { onItemClick(item.id) }
//            }
        }
    }
}