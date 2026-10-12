package com.subway.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.subway.app.data.settings.ThemeMode

@Composable
fun SubwayTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val useDark = ThemePolicy.useDarkTheme(themeMode, isSystemInDarkTheme())
    MaterialTheme(
        colorScheme = if (useDark) MetroDarkColorScheme else GaoDeColorScheme,
        typography = AppTypography.scaled(fontScale),
        content = content
    )
}

private fun Typography.scaled(scale: Float): Typography {
    fun TextStyle.scale() = copy(
        fontSize = (fontSize.value * scale).sp,
        lineHeight = (lineHeight.value * scale).sp
    )
    return copy(
        bodyLarge = bodyLarge.scale(),
        bodyMedium = bodyMedium.scale(),
        titleLarge = titleLarge.scale(),
        titleMedium = titleMedium.scale(),
        headlineSmall = headlineSmall.scale(),
        labelSmall = labelSmall.scale()
    )
}
