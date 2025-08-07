package edu.bluejack24_2.ecoai.ui.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bluejack24_2.ecoai.utils.LanguageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage by LanguageManager.currentLanguage
    val context = LocalContext.current

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clickable { expanded = true }
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                val flagRes = when (currentLanguage) {
                    "EN" -> edu.bluejack24_2.ecoai.R.drawable.us_flag
                    "ID" -> edu.bluejack24_2.ecoai.R.drawable.id_flag
                    else -> edu.bluejack24_2.ecoai.R.drawable.us_flag
                }
                androidx.compose.foundation.Image(
                    painter = painterResource(id = flagRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = currentLanguage,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (expanded) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { expanded = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LanguageManager.getString("select_language"),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 20.sp
                                )
                                IconButton(
                                    onClick = { expanded = false },
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(50)
                                        )
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                            val languages = listOf(
                                Triple("EN", "English", edu.bluejack24_2.ecoai.R.drawable.us_flag),
                                Triple("ID", "Indonesia", edu.bluejack24_2.ecoai.R.drawable.id_flag)
                            )
                            languages.forEach { (code, name, flag) ->
                                val isSelected = code == currentLanguage
                                val bgColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant
                                val textColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            bgColor,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            LanguageManager.setLanguage(code)
                                            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                                .edit { putString("language", code) }
                                            expanded = false
                                        }
                                        .padding(vertical = 16.dp, horizontal = 12.dp)
                                        .align(androidx.compose.ui.Alignment.CenterHorizontally),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        color = textColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = flag),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
