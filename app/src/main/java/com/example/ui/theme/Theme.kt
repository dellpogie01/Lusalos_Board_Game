package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LusalosDarkColorScheme = darkColorScheme(
    primary = NarraGoldPrimary,
    onPrimary = Color(0xFF191433),
    primaryContainer = NarraGoldDark,
    onPrimaryContainer = Color(0xFFEEE7FF),
    secondary = BanigSand,
    onSecondary = Color(0xFF111B35),
    secondaryContainer = WoodCardSurface,
    onSecondaryContainer = BanigSand,
    tertiary = CapizPearl,
    onTertiary = Color(0xFF111B35),
    background = WoodDarkBackground,
    onBackground = BanigSand,
    surface = WoodDarkSurface,
    onSurface = BanigSand,
    surfaceVariant = WoodCardSurface,
    onSurfaceVariant = BanigSandMuted,
    outline = WoodCardBorder,
    error = DangerCrimson,
    onError = Color.White
)

private val LusalosLightColorScheme = lightColorScheme(
    primary = NarraGoldSecondary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEE7FF),
    onPrimaryContainer = Color(0xFF191433),
    secondary = Color(0xFF6451AD),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E3FF),
    onSecondaryContainer = Color(0xFF111B35),
    tertiary = Color(0xFF7F52FF),
    onTertiary = Color.White,
    background = Color(0xFFF4F5FF),
    onBackground = Color(0xFF111B35),
    surface = Color(0xFFECEFFC),
    onSurface = Color(0xFF111B35),
    surfaceVariant = Color(0xFFDFE4F5),
    onSurfaceVariant = Color(0xFF47516E),
    outline = Color(0xFF7885A8),
    error = DangerCrimson,
    onError = Color.White
)

@Composable
fun LusalosTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LusalosDarkColorScheme else LusalosLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
