package com.moraura.soilapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenPrimaryContainer,
    onPrimaryContainer = ForestGreenOnPrimaryContainer,
    secondary = MossGreenSecondary,
    onSecondary = MossGreenOnSecondary,
    secondaryContainer = MossGreenSecondaryContainer,
    onSecondaryContainer = MossGreenOnSecondaryContainer,
    tertiary = LimeHighlightTertiary,
    onTertiary = LimeHighlightOnTertiary,
    tertiaryContainer = LimeHighlightTertiaryContainer,
    onTertiaryContainer = LimeHighlightOnTertiaryContainer,
    background = DarkForestBackground,
    onBackground = DarkForestOnBackground,
    surface = DarkForestSurface,
    onSurface = DarkForestOnSurface,
    surfaceVariant = DarkForestSurfaceVariant,
    onSurfaceVariant = DarkForestOnSurfaceVariant,
    surfaceContainer = DarkForestSurfaceContainer,
    surfaceContainerHigh = DarkForestSurfaceContainerHigh,
    outline = DarkForestOutline,
    outlineVariant = DarkForestOutlineVariant,
    error = AlertRed,
    errorContainer = AlertRedContainer
)

private val LightColorScheme = lightColorScheme(
    primary = LightForestPrimary,
    onPrimary = LightForestOnPrimary,
    primaryContainer = LightForestPrimaryContainer,
    onPrimaryContainer = LightForestOnPrimaryContainer,
    secondary = MossGreenSecondary,
    onSecondary = MossGreenOnSecondary,
    secondaryContainer = MossGreenSecondaryContainer,
    onSecondaryContainer = MossGreenOnSecondaryContainer,
    tertiary = LimeHighlightTertiary,
    onTertiary = LimeHighlightOnTertiary,
    tertiaryContainer = LimeHighlightTertiaryContainer,
    onTertiaryContainer = LimeHighlightOnTertiaryContainer,
    background = LightForestBackground,
    onBackground = LightForestOnBackground,
    surface = LightForestSurface,
    onSurface = LightForestOnSurface,
    surfaceVariant = LightForestSurfaceVariant,
    onSurfaceVariant = LightForestOnSurfaceVariant,
    error = AlertRedLight,
    errorContainer = AlertRedContainerLight
)

@Composable
fun SoilAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
