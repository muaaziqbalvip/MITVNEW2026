package com.mitv.master.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MitvBlack = Color(0xFF000000)
val MitvSurface = Color(0xFF141414)
val MitvRed = Color(0xFFE50914)
val MitvRedDark = Color(0xFFB20710)
val MitvGold = Color(0xFFD4AF37)
val MitvGoldLight = Color(0xFFF2D571)
val MitvTextSecondary = Color(0xFFB3B3B3)

private val MitvDarkColorScheme = darkColorScheme(
    primary = MitvRed,
    secondary = MitvRedDark,
    background = MitvBlack,
    surface = MitvSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = MitvRed
)

@Composable
fun MITVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MitvDarkColorScheme,
        typography = MitvTypography,
        content = content
    )
}