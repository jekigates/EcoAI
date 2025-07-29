package com.bluejack242.ecoai.ui.component
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun WeeklyStreakView(
    streakCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Weekly Streak",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "$streakCount days",
            fontSize = 16.sp
        )
    }
}