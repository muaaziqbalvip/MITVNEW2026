package com.mitv.master.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// MITV brand palette — true Netflix-style: pure black background, red primary accent.
val MitvBlack = Color(0xFF000000)
val MitvSurface = Color(0xFF141414)
val MitvSurfaceElevated = Color(0xFF1F1F1F)
val MitvSurfaceHigh = Color(0xFF2A2A2A)
val MitvRed = Color(0xFFE50914)
val MitvRedDark = Color(0xFFB20710)
val MitvRedBright = Color(0xFFFF1B2D)
val MitvGold = Color(0xFFD4AF37)
val MitvGoldLight = Color(0xFFF2D571)
val MitvTextSecondary = Color(0xFFB3B3B3)
val MitvTextTertiary = Color(0xFF737373)
val MitvBorder = Color(0xFF2E2E2E)
val MitvLiveGreen = Color(0xFF2ECC71)

object MitvGradients {
    val heroFade = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0xCC000000), MitvBlack)
    )
    val topFade = Brush.verticalGradient(
        colors = listOf(Color(0xAA000000), Color.Transparent)
    )
    val cardShine = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0x99000000))
    )
    val redAccent = Brush.horizontalGradient(
        colors = listOf(MitvRedDark, MitvRed, MitvRedBright)
    )
    val goldAccent = Brush.horizontalGradient(
        colors = listOf(MitvGold, MitvGoldLight)
    )
}

private val MitvDarkColorScheme = darkColorScheme(
    primary = MitvRed,
    secondary = MitvRedDark,
    background = MitvBlack,
    surface = MitvSurface,
    surfaceVariant = MitvSurfaceElevated,
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
