package com.bluejack242.ecoai.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage by LanguageManager.currentLanguage

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clickable { expanded = true }
                .background(backgroundColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = currentLanguage,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("EN", "ID").forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang) },
                    onClick = {
                        LanguageManager.setLanguage(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}
