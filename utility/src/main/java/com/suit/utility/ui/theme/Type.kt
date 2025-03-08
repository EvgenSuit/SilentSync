package com.suit.utility.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.suit.utility.R

val bodyFontFamily = FontFamily(
    Font(
        R.font.zilla_slab_regular,
        style = FontStyle.Italic
    )
)

val displayFontFamily = FontFamily(
    Font(
        R.font.alatsi_regular
    )
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(
        fontFamily = displayFontFamily
    ),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = TextStyle(
        fontFamily = displayFontFamily,
        fontSize = 35.sp),

    titleSmall = TextStyle(
        fontFamily = displayFontFamily,
        fontSize = 30.sp),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontSize = 20.sp
    ),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)

