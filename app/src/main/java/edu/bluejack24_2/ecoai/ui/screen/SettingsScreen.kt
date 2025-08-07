package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar
import edu.bluejack24_2.ecoai.ui.component.LanguageSelector
import edu.bluejack24_2.ecoai.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { userDoc ->
                    isNotificationEnabled = userDoc.getBoolean("notificationsEnabled") ?: true
                    isLoading = false
                }
                .addOnFailureListener {
                    isNotificationEnabled = true
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("settings"),
            navController = navController
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Push notifications
                SettingItem {
                    Column(Modifier.weight(1f)) {
                        Text(
                            LanguageManager.getString("push_notifications"),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            LanguageManager.getString("push_notifications_desc"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { enabled ->
                            isNotificationEnabled = enabled
                            if (currentUserId != null) {
                                db.collection("users")
                                    .document(currentUserId)
                                    .update("notificationsEnabled", enabled)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
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
                            if (isDarkTheme) LanguageManager.getString("dark_mode") else LanguageManager.getString(
                                "light_mode"
                            ),
                            color = MaterialTheme.colorScheme.primary
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
                        .navigationBarsPadding()
                ) {
                    Text(LanguageManager.getString("logout"), color = Color.Red)
                }
            }
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
