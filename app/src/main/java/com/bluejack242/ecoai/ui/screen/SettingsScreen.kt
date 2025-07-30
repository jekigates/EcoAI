package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bluejack242.ecoai.ui.component.LanguageSelector
import com.bluejack242.ecoai.utils.LanguageManager

@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    isNotificationEnabled: Boolean,
    onNotificationChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = LanguageManager.getString("settings"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // Push notifications
        SettingItem {
            Column(Modifier.weight(1f)) {
                Text(LanguageManager.getString("push_notifications"), fontWeight = FontWeight.Bold)
                Text(
                    LanguageManager.getString("push_notifications_desc"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
            Switch(
                checked = isNotificationEnabled,
                onCheckedChange = { onNotificationChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF388E3C),      // Warna tombol switch ketika aktif
                    checkedTrackColor = Color(0xFF388E3C).copy(alpha = 0.4f), // Warna track
                )
            )
        }


        // Appearance
        SettingItem {
            Column(Modifier.weight(1f)) {
                Text(LanguageManager.getString("appearance"), fontWeight = FontWeight.Bold)
                Text(
                    LanguageManager.getString("appearance_desc"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
            TextButton(onClick = { onThemeChange(!isDarkTheme) }) {
                Text(
                    if (isDarkTheme) LanguageManager.getString("dark_mode") else LanguageManager.getString("light_mode"),
                    color = Color(0xFF388E3C)
                )
            }
        }


        // Language
        SettingItem {
            Column(Modifier.weight(1f)) {
                Text(LanguageManager.getString("language"), fontWeight = FontWeight.Bold)
                Text(
                    LanguageManager.getString("select_language"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
            LanguageSelector()
        }

        Spacer(modifier = Modifier.weight(1f))

        // Log Out
        TextButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(LanguageManager.getString("logout"), color = Color.Red)
        }

    }
}

@Composable
private fun SettingItem(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
