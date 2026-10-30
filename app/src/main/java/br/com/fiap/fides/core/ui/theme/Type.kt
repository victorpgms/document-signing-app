package br.com.fiap.fides.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import br.com.fiap.fides.R

private val FidesInter = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.Medium),
    Font(R.font.inter_variable, FontWeight.SemiBold),
)

internal val FidesTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FidesInter,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.52).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FidesInter,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.33).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FidesInter,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.22).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FidesInter,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontFamily = FidesInter,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = TextStyle(
        fontFamily = FidesInter,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.08).sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FidesInter,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FidesInter,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.12.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FidesInter,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = TextStyle(
        fontFamily = FidesInter,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = TextStyle(
        fontFamily = FidesInter,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)
