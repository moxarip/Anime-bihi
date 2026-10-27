package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val AnimeBihiDarkColorScheme = darkColorScheme(
    primary = ElectricCrimson,
    onPrimary = TextPrimary,
    primaryContainer = CyberPurple,
    onPrimaryContainer = TextPrimary,
    secondary = CyberPurple,
    onSecondary = TextPrimary,
    secondaryContainer = ObsidianElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = CyberCyan,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = RichObsidian,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

@Composable
fun AnimeBihiTheme(
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = AnimeBihiDarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    AnimeBihiTheme(isRtl = true, content = content)
}
