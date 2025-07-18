package com.bluejack242.ecoai.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import com.bluejack242.ecoai.model.LoginRequest

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = remember { AuthViewModel() },
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val error = viewModel.errorMessage.value
    val isLoading = viewModel.isLoading.value
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Language Selector
        LanguageSelector(
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp),
            backgroundColor = Color(0xFFF5F5F5),
            textColor = Color.Black
        )
        
        // Gambar placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("GAMBAR", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = LanguageManager.getString("welcome_back"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(LanguageManager.getString("email_address")) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(LanguageManager.getString("password_hint")) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = LanguageManager.getString("forgotten_password"),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.login(LoginRequest(email, password)) {
                Toast.makeText(context, LanguageManager.getString("login_success"), Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(LanguageManager.getString("log_in"), color = Color.White, fontWeight = FontWeight.Bold)
        }
        if (error != null) {
            Text(error, color = Color.Red)
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(LanguageManager.getString("dont_have_account"), color = Color(0xFF888888))
            TextButton(onClick = onRegisterClick) {
                Text(LanguageManager.getString("register"), fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}
