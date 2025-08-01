package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluejack242.ecoai.model.WasteItem
import com.bluejack242.ecoai.viewmodel.WasteViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.bluejack242.ecoai.utils.LanguageManager

@Composable
fun WasteDatabaseScreen(
    navController: NavHostController,
    viewModel: WasteViewModel = viewModel()
) {
    val wasteItems by viewModel.wasteDatabaseItems.observeAsState(emptyList())
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        viewModel.fetchWasteDatabaseItems()
    }

    Column(Modifier.fillMaxSize().padding(16.dp).statusBarsPadding()) {
        Text(
            text = LanguageManager.getString("waste_database"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (wasteItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = LanguageManager.getString("no_items_available"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wasteItems) { item ->
                    WasteItemRow(
                        item = item,
                        onClick = {
                            viewModel.addWasteItemFromDatabase(
                                name = item.name,
                                co2e = item.co2e,
                                imageUrl = item.imageRes,
                                uploadedBy = userId,
                                disposalMethod = item.sortingGuide,
                                onSuccess = { navController.navigateUp() }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WasteItemRow(item: WasteItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageRes,
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(8.dp)
                        clip = true
                    }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.co2e} gram CO2e",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.sortingGuide,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}