package com.bluejack242.ecoai

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.bluejack242.ecoai.ui.theme.EcoAITheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "NOTIF_CHANNEL_ID",
                "EcoAI Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "EcoAI app notifications"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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

            val navController = rememberNavController()
            EcoAITheme(darkTheme = isDarkTheme) {

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
            val postId = intent.getStringExtra("postId")
            val navigateTo = intent.getStringExtra("navigateTo")
            if (navigateTo == "post_detail" && postId != null) {
                navController.navigate("post_detail/$postId")
            }

        }

    }
}