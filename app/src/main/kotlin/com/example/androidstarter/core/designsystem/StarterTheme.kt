package com.example.androidstarter.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF235A48),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EADD),
    onPrimaryContainer = Color(0xFF15382B),
    background = Color(0xFFF8F9F5),
    surface = Color(0xFFF8F9F5),
    surfaceContainer = Color(0xFFEEF1EB),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA7D5B9),
    onPrimary = Color(0xFF12382A),
    primaryContainer = Color(0xFF254C3B),
    onPrimaryContainer = Color(0xFFD5EADD),
    background = Color(0xFF101813),
    surface = Color(0xFF101813),
    surfaceContainer = Color(0xFF1D2821),
)

@Composable
fun StarterTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
