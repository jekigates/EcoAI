package com.bluejack242.ecoai.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt


// define your colors for dark theme
val clear_dark = Color(0xFFA05162)
val dark_btn = Color(0xFF222427)

// define your colors for light theme
val light_btn = Color("#E9F0F4".toColorInt())
val light_bg = Color("#F6F8F9".toColorInt())
val clear_light = Color(0xFFF1C8D1)

val green_primary = Color(0xFF388E3C)
val dark_background = Color(0xFF181C1B)
val dark_surface = Color(0xFF232826)
val light_background = Color(0xFFF6F8F9)
val light_surface = Color(0xFFE9F0F4)

// Subtle outline colors for card borders
val outline_light = Color(0x1A000000) // 10% black
val outline_dark = Color(0x33FFFFFF) // 20% white

sealed class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val text: Color,
    val outline: Color
)  {
    object Night: ThemeColors(
        background = dark_background,
        surface = dark_surface,
        primary = green_primary,
        text = Color.White,
        outline = outline_dark
    )
    object Day: ThemeColors(
        background = light_background,
        surface = light_surface,
        primary = green_primary,
        text = Color.Black,
        outline = outline_light
    )
}