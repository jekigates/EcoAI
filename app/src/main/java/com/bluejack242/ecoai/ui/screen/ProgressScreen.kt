package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluejack242.ecoai.viewmodel.ProgressViewModel
import com.bluejack242.ecoai.ui.component.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.bluejack242.ecoai.utils.LanguageManager

@Composable
fun ProgressScreen(
    navController: NavHostController,
    onAddWasteClick: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: ProgressViewModel = viewModel()
) {
    val recentlyUploaded by viewModel.recentlyUploadedWaste.observeAsState(emptyList())
    val carbonTrack by viewModel.carbonTrack.observeAsState(0)
    val weeklyStreak by viewModel.weeklyStreak.observeAsState(0)

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    DisposableEffect(Unit) {
        viewModel.fetchRecentlyUploadedWaste(userId)
        viewModel.fetchCarbonTrack()
        viewModel.fetchWeeklyStreak()
        onDispose { }
    }


    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fabColor = Color(0xFF4CAF50)
    val fabIconColor = Color.White
    val subtitleColor = onSurfaceVariantColor
    val emptyTextColor = onSurfaceVariantColor

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = "progress"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWasteClick,
                containerColor = fabColor,
                contentColor = fabIconColor
            ) {
                Icon(Icons.Default.Add, contentDescription = LanguageManager.getString("add_waste"), tint = fabIconColor)
            }
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(backgroundColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString("progress_title"),
                    style = MaterialTheme.typography.titleLarge,
                    color = onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Weekly streak
            WeeklyStreakView(streakCount = weeklyStreak)

            // Carbon track (items left)
            Spacer(Modifier.height(8.dp))
            CarbonTrackView(carbonFootprint = carbonTrack, itemsUploaded = recentlyUploaded.size)

            Spacer(Modifier.height(16.dp))

            // Recently uploaded title
            Text(LanguageManager.getString("recently_uploaded"), style = MaterialTheme.typography.titleMedium, color = subtitleColor)
            Spacer(Modifier.height(8.dp))

            // List uploaded items
            if (recentlyUploaded.isEmpty()) {
                Text(LanguageManager.getString("no_items_uploaded"), color = emptyTextColor)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(recentlyUploaded.size) { index ->
                        RecentlyUploadedItemCard(
                            item = recentlyUploaded[index],
                            onClick = { onItemClick(it) }
                        )
                    }
                }
            }
        }
    }
}
