package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.bluejack24_2.ecoai.model.WasteHistoryItem
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.viewmodel.WasteViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: androidx.navigation.NavHostController,
    onItemClick: (String) -> Unit,
    viewModel: WasteViewModel = viewModel()
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val historyList by viewModel.recentlyUploadedWaste.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.fetchHistory(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("history"),
            navController = navController
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text(LanguageManager.getString("search_history")) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        val filteredHistory = historyList.filter { item ->
            item.name.contains(searchQuery, ignoreCase = true) ||
                    item.date.toString().contains(searchQuery, ignoreCase = true)
        }

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) LanguageManager.getString("no_history_yet") else LanguageManager.getString("no_results_found"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredHistory) { item ->
                    HistoryItemRow(item, onItemClick)
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: WasteHistoryItem, onItemClick: (String) -> Unit) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
    val formattedDate = dateFormat.format(item.date.toDate())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.id) },
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
                    .size(64.dp)
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.co2e} gram CO2e",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
        }
    }
}