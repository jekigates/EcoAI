package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import edu.bluejack24_2.ecoai.ui.component.CustomDialog
import edu.bluejack24_2.ecoai.ui.component.DialogType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import edu.bluejack24_2.ecoai.model.RegisterRequest
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.ui.component.LanguageSelector
import edu.bluejack24_2.ecoai.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onLoginClick: () -> Unit
) {
    val error = viewModel.errorMessage.value
    val isLoading = viewModel.isLoading.value

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // Language Selector
        LanguageSelector(
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = LanguageManager.getString("register"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(LanguageManager.getString("email_address")) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmEmail,
            onValueChange = { confirmEmail = it },
            label = { Text(LanguageManager.getString("confirm_email")) },
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(LanguageManager.getString("confirm_password")) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.register(RegisterRequest(
                    firstName,
                    lastName,
                    email,
                    confirmEmail,
                    password,
                    confirmPassword)
                ) {
                    showSuccessDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(LanguageManager.getString("create_account"), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(LanguageManager.getString("have_account"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onLoginClick) {
                Text(LanguageManager.getString("log_in"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }


        if (isLoading) {
            CircularProgressIndicator()
        }

        if (showSuccessDialog) {
            CustomDialog(
                title = LanguageManager.getString("registration_success"),
                message = LanguageManager.getString("verification_link_sent"),
                confirmText = LanguageManager.getString("go_to_login"),
                onConfirm = {
                    showSuccessDialog = false
                    onLoginClick()
                },
                onDismiss = {},
                dialogType = DialogType.Success
            )
        }
    }
}
