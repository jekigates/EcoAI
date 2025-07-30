package com.bluejack242.ecoai.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.rememberCoroutineScope
import com.bluejack242.ecoai.utils.LanguageManager
import com.bluejack242.ecoai.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(viewModel: AuthViewModel, navController: NavController) {
    val email = remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val showSuccessDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // typical app bar height
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = LanguageManager.getString("forgot_password_title"),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = LanguageManager.getString("back"))
            }
        }
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text(LanguageManager.getString("enter_email_address")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.sendResetEmail(email.value) {
                    coroutineScope.launch {
                        showSuccessDialog.value = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(LanguageManager.getString("send_reset_link"), color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (showSuccessDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog.value = false
                    navController.navigate("login")
                },
                title = { Text(LanguageManager.getString("success")) },
                text = { Text(LanguageManager.getString("reset_password_success_message")) },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog.value = false
                            navController.navigate("login")
                        }
                    ) {
                        Text(LanguageManager.getString("ok"))
                    }
                }
            )
        }
    }
}
