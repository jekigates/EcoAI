package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluejack242.ecoai.viewmodel.ProgressViewModel
import com.bluejack242.ecoai.ui.component.*
import com.bluejack242.ecoai.ui.component.WeeklyProgressRow
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.bluejack242.ecoai.utils.LanguageManager
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import com.bluejack242.ecoai.R

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
        viewModel.fetchCarbonTrack(userId)
        viewModel.fetchWeeklyStreak(userId)
        onDispose { }
    }


    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fabColor = MaterialTheme.colorScheme.primary
    val fabIconColor = Color.White
    // val emptyTextColor = onSurfaceVariantColor

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
                Icon(
                    Icons.Default.Add,
                    contentDescription = LanguageManager.getString("add_waste"),
                    tint = fabIconColor
                )
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
            // Progress title and streak indicator row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString("progress_title"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = onSurfaceColor,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )
                // Streak indicator styled like language selector
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_local_fire_department_24),
                            contentDescription = "Streak",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = weeklyStreak.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Weekly progress row (custom)
            WeeklyProgressRow()

            Spacer(Modifier.height(24.dp))

            // Carbon track (items left)
            CarbonTrackView(carbonFootprint = carbonTrack, itemsUploaded = recentlyUploaded.size)

            Spacer(Modifier.height(24.dp))

            // Recently uploaded title
            Text(
                LanguageManager.getString("recently_uploaded"),
                style = MaterialTheme.typography.titleMedium,
                color = onSurfaceVariantColor
            )
            Spacer(Modifier.height(8.dp))

            // List uploaded items
            if (recentlyUploaded.isEmpty()) {
                Text(LanguageManager.getString("no_items_uploaded"), color = onSurfaceVariantColor)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(recentlyUploaded.size) { index ->
                        RecentlyUploadedItemCard(
                            item = recentlyUploaded[index],
                            onClick = { onItemClick(recentlyUploaded[index].id) }
                        )
                    }
                }
            }
        }
    }
}