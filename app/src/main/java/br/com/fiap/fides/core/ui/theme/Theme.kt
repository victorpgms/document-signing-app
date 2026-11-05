package br.com.fiap.fides.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val FidesLightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = LightSurface,
    secondary = BrandBlue,
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
    primary = BrandGold,
    onPrimary = DarkBackground,
    secondary = DarkBrandBlue,
    onSecondary = DarkBackground,
    tertiary = BrandGold,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Error,
)

private val FidesShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
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
        shapes = FidesShapes,
        content = content,
    )
}
