package com.bluejack242.ecoai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluejack242.ecoai.ui.auth.LandingScreen
import com.bluejack242.ecoai.ui.theme.EcoAITheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // State untuk toggle tema
            var isDarkTheme by remember { mutableStateOf(false) }
            var showLanding by remember { mutableStateOf(true) }

            EcoAITheme(darkTheme = isDarkTheme) {
                Surface (
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showLanding) {
                        LandingScreen (
                            onLoginClick = { /* TODO: Navigate to login */ },
                            onGetStartedClick = { /* TODO: Navigate to register */ }
                        )
                    } else {
                    Column (
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (isDarkTheme) "Dark Theme Active" else "Light Theme Active",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isDarkTheme = !isDarkTheme }
                        ) {
                            Text("Toggle Theme")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {}) {
                            Text("A BUTTON")
                        }
                    }}
                }
            }
        }
    }
}