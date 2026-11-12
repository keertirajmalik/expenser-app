package com.expenser.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColors = lightColorScheme(
    primary = RoseLightPrimary,
    onPrimary = RoseLightPrimaryFg,
    // FABs and the time picker fill with the container slots; left unset they fall
    // back to Material's baseline lilac, off-palette in both themes.
    primaryContainer = RoseLightPrimary,
    onPrimaryContainer = RoseLightPrimaryFg,
    secondaryContainer = RoseLightSelected,
    onSecondaryContainer = RoseLightForeground,
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
    // Bottom sheets paint with surfaceContainerLow: keep them white over the grey ground.
    surfaceContainerLow = RoseLightCard,
    surfaceContainerHigh = RoseLightSecondary,
    // Switch off-track and filled inputs; unset it is Material's lavender #E6E0E9.
    surfaceContainerHighest = RoseLightMuted,
    error = RoseLightDestructive,
    onError = RoseLightDestructiveFg,
    outline = RoseLightOutline,
    outlineVariant = RoseLightBorder,
)

private val DarkColors = darkColorScheme(
    primary = RoseDarkPrimary,
    onPrimary = RoseDarkPrimaryFg,
    // FABs and the time picker fill with the container slots; left unset they fall
    // back to Material's baseline lilac, off-palette in both themes.
    primaryContainer = RoseDarkPrimary,
    onPrimaryContainer = RoseDarkPrimaryFg,
    secondaryContainer = RoseDarkSelected,
    onSecondaryContainer = RoseDarkForeground,
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
    // Bottom sheets paint with surfaceContainerLow: lift them off the near-black ground.
    surfaceContainerLow = RoseDarkCard,
    surfaceContainerHigh = RoseDarkSecondary,
    surfaceContainerHighest = RoseDarkMuted,
    error = RoseDarkDestructive,
    onError = RoseDarkDestructiveFg,
    outline = RoseDarkOutline,
    outlineVariant = RoseDarkBorder,
)

/**
 * Whether [ExpenserTheme] is showing its dark scheme. Components with colours the
 * Material scheme has no slot for (category tints, the hero card) pick their pair from
 * this rather than guessing from a scheme colour.
 */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun ExpenserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
