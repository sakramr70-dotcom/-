package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    secondary = AccentPurple,
    tertiary = AccentGold,
    background = BgDarkPrimary,
    surface = BgDarkCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AccentOrange,
    secondary = AccentPurple,
    tertiary = AccentGold,
    background = BgLightPrimary,
    surface = BgLightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BgDarkPrimary,
    onSurface = BgDarkPrimary
)

@Composable
fun NujoomTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
