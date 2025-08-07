package edu.bluejack24_2.ecoai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColorScheme(
    primary = ThemeColors.Night.primary,
    onPrimary = ThemeColors.Night.text,
    surface = ThemeColors.Night.surface,
    surfaceVariant = ThemeColors.Night.surfaceVariant,
    background = ThemeColors.Night.background,
    outline = ThemeColors.Night.outline
)

private val LightColorPalette = lightColorScheme(
    primary = ThemeColors.Day.primary,
    onPrimary = ThemeColors.Day.text,
    surface = ThemeColors.Day.surface,
    surfaceVariant = ThemeColors.Day.surfaceVariant,
    background = ThemeColors.Day.background,
    outline = ThemeColors.Day.outline
)

@Composable
fun EcoAITheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}