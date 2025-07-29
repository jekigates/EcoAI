package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
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
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

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
                backgroundColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Waste", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Title
            Text("Progress", style = MaterialTheme.typography.h4)

            Spacer(Modifier.height(8.dp))

            // Weekly streak
            WeeklyStreakView(streakCount = weeklyStreak)

            // Carbon track (items left)
            Spacer(Modifier.height(8.dp))
            CarbonTrackView(carbonFootprint = carbonTrack, itemsUploaded = recentlyUploaded.size)

            Spacer(Modifier.height(16.dp))

            // Recently uploaded title
            Text("Recently uploaded", style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.height(8.dp))

            // List uploaded items
            if (recentlyUploaded.isEmpty()) {
                Text("No items uploaded yet.", color = Color.Gray)
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
