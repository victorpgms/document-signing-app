package br.com.fiap.fides.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FidesLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = LightSurface,
    secondary = BrandPurple,
    onSecondary = LightSurface,
    tertiary = BrandGold,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceMuted,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = Error,
)

private val FidesDarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = LightSurface,
    secondary = BrandPurple,
    onSecondary = LightSurface,
    tertiary = BrandGold,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkFieldSurface,
    error = Error,
)

@Composable
fun FidesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) FidesDarkColorScheme else FidesLightColorScheme

    // Dynamic color stays disabled in Sprint 1 so the brand palette remains stable.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FidesTypography,
        content = content,
    )
}
