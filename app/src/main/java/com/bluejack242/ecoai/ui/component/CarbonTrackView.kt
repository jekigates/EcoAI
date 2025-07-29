package com.bluejack242.ecoai.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CarbonTrackView(carbonFootprint: Int, itemsUploaded: Int) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Carbon Footprint", style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.height(4.dp))
            Text("$carbonFootprint gram CO2e", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(4.dp))
            Text("$itemsUploaded items uploaded")
        }
    }
}
