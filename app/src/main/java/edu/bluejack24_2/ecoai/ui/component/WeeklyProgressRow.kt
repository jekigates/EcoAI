package edu.bluejack24_2.ecoai.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import java.util.Calendar
import edu.bluejack24_2.ecoai.utils.LanguageManager
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun WeeklyProgressRow(
    uploadDays: Set<Long> // pass set of millis for days with uploads
) {
    val dayLettersEn = listOf("S", "M", "T", "W", "T", "F", "S")
    val dayLettersId = listOf("M", "S", "S", "R", "K", "J", "S") // Minggu, Senin, Selasa, Rabu, Kamis, Jumat, Sabtu
    val dayLetters = if (LanguageManager.currentLanguage.value == "ID") dayLettersId else dayLettersEn
    val days = mutableListOf<String>()
    val dates = mutableListOf<Int>()
    val dateMillisList = mutableListOf<Long>()
    for (i in 27 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -i)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        days.add(dayLetters[dow - 1])
        dates.add(cal.get(Calendar.DAY_OF_MONTH))
        // Store midnight millis for this day
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        dateMillisList.add(cal.timeInMillis)
    }
    val pageSize = 7
    val pageCount = (days.size + pageSize - 1) / pageSize
    val lastPage = pageCount - 1
    val pagerState = rememberPagerState(initialPage = lastPage) { pageCount }
    var selectedIndex by remember { mutableIntStateOf(days.size - 1) } // default today
    Spacer(Modifier.height(8.dp))
    val dayCircleBg = MaterialTheme.colorScheme.surfaceVariant
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        val start = page * pageSize
        val end = minOf(start + pageSize, days.size)
        val daysPage = days.subList(start, end)
        val datesPage = dates.subList(start, end)
        Column {
            val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysPage.forEachIndexed { i, day ->
                    val globalIndex = start + i
                    val isSelected = globalIndex == selectedIndex
                    val dayMillis = dateMillisList[globalIndex]
                    val hasUpload = uploadDays.contains(dayMillis)
                    // Use the same red as the fire icon in ProgressScreen
                    val fireRed = Color.Red
                    val dashColor = if (isSelected) outlineColor else if (hasUpload) fireRed else outlineColor
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) dayCircleBg else Color.Transparent)
                            .clickable { selectedIndex = globalIndex },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.matchParentSize()
                        ) {
                            drawCircle(
                                color = dashColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                                )
                            )
                        }
                        Text(
                            day,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                datesPage.forEachIndexed { i, date ->
                    val globalIndex = start + i
                    val isSelected = globalIndex == selectedIndex
                    Text(
                        date.toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
    // Expose selected date in millis to parent via callback
    LaunchedEffect(selectedIndex) {
        WeeklyProgressRowSelectedDate.value = dateMillisList[selectedIndex]
    }
}

// State holder for selected date in millis
val WeeklyProgressRowSelectedDate = mutableStateOf<Long?>(null)
