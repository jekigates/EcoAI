package edu.bluejack24_2.ecoai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import edu.bluejack24_2.ecoai.ui.component.CustomDialog
import edu.bluejack24_2.ecoai.ui.component.DialogType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.rememberCoroutineScope
import edu.bluejack24_2.ecoai.ui.component.BackHeaderBar
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(viewModel: AuthViewModel, navController: NavHostController) {
    val email = remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val showSuccessDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackHeaderBar(
            title = LanguageManager.getString("forgot_password_title"),
            navController = navController
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(LanguageManager.getString("send_reset_link"), color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(Modifier.height(16.dp))

            if (!errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (showSuccessDialog.value) {
                CustomDialog(
                    title = LanguageManager.getString("success"),
                    message = LanguageManager.getString("reset_password_success_message"),
                    confirmText = LanguageManager.getString("ok"),
                    onConfirm = {
                        showSuccessDialog.value = false
                    },
                    onDismiss = {
                        showSuccessDialog.value = false
                    },
                    dialogType = DialogType.Success
                )
            }
        }
    }
}
