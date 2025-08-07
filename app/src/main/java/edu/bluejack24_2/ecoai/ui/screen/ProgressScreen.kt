package edu.bluejack24_2.ecoai.ui.screen

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
import edu.bluejack24_2.ecoai.viewmodel.ProgressViewModel
import edu.bluejack24_2.ecoai.ui.component.*
import edu.bluejack24_2.ecoai.ui.component.WeeklyProgressRow
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.filled.AccessTimeFilled

@Composable
fun ProgressScreen(
    navController: NavHostController,
    onItemClick: (String) -> Unit,
    viewModel: ProgressViewModel = viewModel()
) {
    val recentlyUploadedAll by viewModel.recentlyUploadedWaste.observeAsState(emptyList())
    val selectedDateMillis = WeeklyProgressRowSelectedDate.value
    // Calculate set of upload days (midnight millis) for the last 28 days
    val uploadDays: Set<Long> = remember(recentlyUploadedAll) {
        recentlyUploadedAll.map { item ->
            val cal = java.util.Calendar.getInstance()
            cal.time = item.date.toDate()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet()
    }
    val recentlyUploaded = remember(recentlyUploadedAll, selectedDateMillis) {
        if (selectedDateMillis == null) emptyList() else {
            val end = selectedDateMillis + 24 * 60 * 60 * 1000
            recentlyUploadedAll.filter { item ->
                val date = item.date.toDate().time
                date >= selectedDateMillis && date < end
            }
        }
    }
    // Show carbon for selected date only
    val carbonTrack = recentlyUploaded.sumOf { it.co2e }
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

    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = "progress"
            )
        },
        floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    AnimatedVisibility(
                        visible = fabExpanded,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    navController.navigate("add_waste")
                                },
                                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                text = { Text(LanguageManager.getString("scan_waste")) },
                                containerColor = fabColor,
                                contentColor = fabIconColor,
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                            )
                            ExtendedFloatingActionButton(
                                onClick = { fabExpanded = false
                                    navController.navigate("waste_database")},
                                icon = { Icon(painterResource(id = R.drawable.baseline_recycling_24), contentDescription = null) },
                                text = { Text(LanguageManager.getString("waste_database")) },
                                containerColor = fabColor,
                                contentColor = fabIconColor,
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                            )
                            ExtendedFloatingActionButton(
                                onClick = { fabExpanded = false
                                    navController.navigate("history")},
                                icon = {Icon(Icons.Filled.AccessTimeFilled, contentDescription = null)},
                                text = { Text(LanguageManager.getString("open_history"))},
                                containerColor = fabColor,
                                contentColor = fabIconColor,
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                            )
                        }
                    }
                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        containerColor = fabColor,
                        contentColor = fabIconColor
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = LanguageManager.getString("add_waste"),
                            tint = fabIconColor
                        )
                    }
                }
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Weekly progress row
            WeeklyProgressRow(uploadDays = uploadDays)

            Spacer(Modifier.height(24.dp))

            // Carbon track
            CarbonTrackView(carbonFootprint = carbonTrack)

            Spacer(Modifier.height(24.dp))

            // Recently uploaded title
            Text(
                LanguageManager.getString("recently_uploaded"),
                style = MaterialTheme.typography.titleMedium,
                color = onSurfaceVariantColor,
                fontWeight = FontWeight.Bold
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