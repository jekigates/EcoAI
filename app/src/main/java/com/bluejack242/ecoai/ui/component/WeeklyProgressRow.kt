package com.bluejack242.ecoai.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import java.util.Calendar
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
    // Example: T F S S M T W, dates 3-9, highlight current day (today is Wednesday, index 6)
    val days = listOf("T", "F", "S", "S", "M", "T", "W")
    val dates = listOf(3, 4, 5, 6, 7, 8, 9)
    val todayIndex = 6 // For demo, highlight last box (Wednesday)
    val todayDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { i, day ->
            val isToday = i == todayIndex
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isToday) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    day,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dates.forEachIndexed { i, date ->
            val isTodayDate = date == todayDate
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
