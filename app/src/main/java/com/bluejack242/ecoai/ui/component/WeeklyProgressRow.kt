package com.bluejack242.ecoai.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import java.util.Calendar
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeeklyProgressRow() {
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")
    val days = mutableListOf<String>()
    val dates = mutableListOf<Int>()
    for (i in 27 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -i)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        days.add(dayLetters[dow - 1])
        dates.add(cal.get(Calendar.DAY_OF_MONTH))
    }
    val pageSize = 7
    val pageCount = (days.size + pageSize - 1) / pageSize
    val lastPage = pageCount - 1
    val pagerState = rememberPagerState(initialPage = lastPage) { pageCount }
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
        val todayIndex = if (page == lastPage) (daysPage.size - 1) else -1
        Column {
            val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysPage.forEachIndexed { i, day ->
                    val isToday = i == todayIndex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isToday) dayCircleBg else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.matchParentSize()
                        ) {
                            drawCircle(
                                color = outlineColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                                )
                            )
                        }
                        Text(
                            day,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
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
                    val isTodayDate = (page == lastPage) && (i == todayIndex)
                    Text(
                        date.toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isTodayDate) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}
