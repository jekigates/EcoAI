package com.bluejack242.ecoai.ui.component
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluejack242.ecoai.model.WasteHistoryItem
import com.bluejack242.ecoai.model.WasteItem

@Composable
fun WasteItemRow(
    item: WasteHistoryItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item.name)
        Text(text = "${item.co2e} g CO2e")
    }
}