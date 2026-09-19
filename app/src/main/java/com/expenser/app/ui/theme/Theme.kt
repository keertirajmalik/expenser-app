package com.expenser.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = RoseLightPrimary,
    onPrimary = RoseLightPrimaryFg,
    secondary = RoseLightSecondary,
    onSecondary = RoseLightSecondaryFg,
    tertiary = RoseLightSecondary,
    onTertiary = RoseLightSecondaryFg,
    background = RoseLightBackground,
    onBackground = RoseLightForeground,
    surface = RoseLightCard,
    onSurface = RoseLightForeground,
    surfaceVariant = RoseLightMuted,
    onSurfaceVariant = RoseLightMutedFg,
    surfaceContainer = RoseLightMuted,
    surfaceContainerLow = RoseLightBackground,
    surfaceContainerHigh = RoseLightSecondary,
    error = RoseLightDestructive,
    onError = RoseLightDestructiveFg,
    outline = RoseLightBorder,
    outlineVariant = RoseLightBorder,
)

private val DarkColors = darkColorScheme(
    primary = RoseDarkPrimary,
    onPrimary = RoseDarkPrimaryFg,
    secondary = RoseDarkSecondary,
    onSecondary = RoseDarkSecondaryFg,
    tertiary = RoseDarkSecondary,
    onTertiary = RoseDarkSecondaryFg,
    background = RoseDarkBackground,
    onBackground = RoseDarkForeground,
    surface = RoseDarkCard,
    onSurface = RoseDarkForeground,
    surfaceVariant = RoseDarkMuted,
    onSurfaceVariant = RoseDarkMutedFg,
    surfaceContainer = RoseDarkCard,
    surfaceContainerLow = RoseDarkBackground,
    surfaceContainerHigh = RoseDarkSecondary,
    error = RoseDarkDestructive,
    onError = RoseDarkDestructiveFg,
    outline = RoseDarkBorder,
    outlineVariant = RoseDarkBorder,
)

@Composable
fun ExpenserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
