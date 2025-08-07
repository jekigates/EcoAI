package edu.bluejack24_2.ecoai.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.bluejack24_2.ecoai.utils.LanguageManager

@Composable
fun CarbonTrackView(carbonFootprint: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Carbon value
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = carbonFootprint.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = LanguageManager.getString("carbon_left") + " | gram CO2e",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Right: Circular progress with fire icon
            val ringColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw ring (stroke only)
                    drawCircle(
                        color = ringColor,
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                    )
                }
                // Cloud icon in center, smaller, same color as ring
                androidx.compose.material3.Icon(
                    painter = androidx.compose.ui.res.painterResource(id = edu.bluejack24_2.ecoai.R.drawable.baseline_cloud_24),
                    contentDescription = "Cloud",
                    tint = ringColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
