package com.bluejack242.ecoai.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

// define your colors for dark theme
val clear_dark = Color(0xFFA05162)
val dark_btn = Color(0xFF222427)

// define your colors for dark theme
val light_btn = Color("#E9F0F4".toColorInt())
val light_bg = Color("#F6F8F9".toColorInt())
val clear_light = Color(0xFFF1C8D1)

sealed class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val text: Color
)  {
    object Night: ThemeColors(
        background = Color.Black,
        surface = dark_btn,
        primary = clear_dark,
        text = Color.White
    )
    object Day: ThemeColors(
        background = light_bg,
        surface = light_btn,
        primary = clear_light,
        text = Color.Black
    )
}