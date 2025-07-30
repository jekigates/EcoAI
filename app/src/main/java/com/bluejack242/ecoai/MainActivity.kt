package com.bluejack242.ecoai

import android.os.Bundle
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
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var isNotificationEnabled by rememberSaveable { mutableStateOf(true) }

            EcoAITheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it },
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