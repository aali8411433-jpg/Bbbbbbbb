package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = ArcCyanPrimary,
    onPrimary = Color(0xFF002029),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFF80F3FF),
    secondary = ArcBlueSecondary,
    onSecondary = Color(0xFF002238),
    secondaryContainer = Color(0xFF003E6B),
    onSecondaryContainer = Color(0xFF90D5FF),
    tertiary = ArcVioletTertiary,
    onTertiary = Color(0xFF21005D),
    tertiaryContainer = Color(0xFF381E72),
    onTertiaryContainer = Color(0xFFCFBCFF),
    background = SpaceNavyDark,
    onBackground = TextPaleIce,
    surface = SpaceNavySurface,
    onSurface = TextPaleIce,
    surfaceVariant = SpaceNavyCard,
    onSurfaceVariant = TextMutedBlue,
    outline = SpaceNavyCardBorder,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
