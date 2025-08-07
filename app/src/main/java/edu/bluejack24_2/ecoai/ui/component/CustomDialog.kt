package edu.bluejack24_2.ecoai.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import androidx.compose.runtime.getValue


enum class DialogType {
    Success, Confirm, Error
}

@Composable
fun CustomDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dialogType: DialogType = DialogType.Success,
    showDismiss: Boolean = false,
    dismissText: String = "Cancel"
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.92f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val lottieRes = when (dialogType) {
                    DialogType.Success -> edu.bluejack24_2.ecoai.R.raw.success
                    DialogType.Confirm -> edu.bluejack24_2.ecoai.R.raw.parsa_loading
                    DialogType.Error -> edu.bluejack24_2.ecoai.R.raw.tomato_error
                }
                val lottieIterations = when (dialogType) {
                    DialogType.Success -> 1
                    DialogType.Confirm -> LottieConstants.IterateForever
                    DialogType.Error -> 1
                }
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = lottieIterations
                )
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .padding(bottom = 8.dp)
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        enableMergePaths = true
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (showDismiss) {
                        TextButton(onClick = onDismiss) {
                            Text(dismissText)
                        }
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
