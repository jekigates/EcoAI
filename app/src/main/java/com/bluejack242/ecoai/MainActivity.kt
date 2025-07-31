package com.bluejack242.ecoai

import android.os.Bundle
import android.content.Context
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bluejack242.ecoai.ui.theme.EcoAITheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            var isDarkTheme by rememberSaveable {
                mutableStateOf(prefs.getBoolean("isDarkTheme", false))
            }
            var isNotificationEnabled by rememberSaveable { mutableStateOf(true) }

            // Load language from prefs if available
            val initialLang = prefs.getString("language", "EN") ?: "EN"
            LaunchedEffect(Unit) {
                com.bluejack242.ecoai.utils.LanguageManager.setLanguage(initialLang)
            }

            EcoAITheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = {
                            isDarkTheme = it
                            prefs.edit().putBoolean("isDarkTheme", it).apply()
                        },
                        isNotificationEnabled = isNotificationEnabled,
                        onNotificationChange = { isNotificationEnabled = it },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("landing") {
                                popUpTo("landing") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

    }
}