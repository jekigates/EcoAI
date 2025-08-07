package edu.bluejack24_2.ecoai.ui.screen

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bluejack24_2.ecoai.model.WasteItem
import edu.bluejack24_2.ecoai.viewmodel.WasteViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar
import edu.bluejack24_2.ecoai.utils.LanguageManager

@Composable
fun WasteDatabaseScreen(
    navController: NavHostController,
    viewModel: WasteViewModel = viewModel()
) {
    val wasteItems by viewModel.wasteDatabaseItems.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        viewModel.fetchWasteDatabaseItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("waste_database"),
            navController = navController
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchWasteDatabaseItems(it)
            },
            placeholder = { Text(LanguageManager.getString("search")) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (wasteItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) LanguageManager.getString("no_items_available")
                    else LanguageManager.getString("no_results_found"),
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageRes,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
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
