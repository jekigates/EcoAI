package com.bluejack242.ecoai.ui.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluejack242.ecoai.utils.LanguageManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier,
    backgroundColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage by LanguageManager.currentLanguage
    val context = LocalContext.current

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clickable { expanded = true }
                .background(backgroundColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                val flagRes = when (currentLanguage) {
                    "EN" -> com.bluejack242.ecoai.R.drawable.us_flag
                    "ID" -> com.bluejack242.ecoai.R.drawable.id_flag
                    else -> com.bluejack242.ecoai.R.drawable.us_flag
                }
                androidx.compose.foundation.Image(
                    painter = painterResource(id = flagRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = currentLanguage,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
        ) {
            listOf("EN", "ID").forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        LanguageManager.setLanguage(lang)
                        // Save language to SharedPreferences
                        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .edit().putString("language", lang).apply()
                        expanded = false
                    }
                )
            }
        }
    }
}
