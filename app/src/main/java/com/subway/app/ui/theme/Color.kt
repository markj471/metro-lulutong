package com.subway.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val MetroBlue = Color(0xFF1677FF)
val MetroBlueSoft = Color(0xFFEAF2FF)
val MetroWhite = Color(0xFFFAFAFA)
val MetroCard = Color(0xFFF5F7FA)
val MetroTextPrimary = Color(0xFF1A1A1A)
val MetroTextSecondary = Color(0xFF8E8E93)
val MetroTransfer = Color(0xFFE53935)
val MetroGreen = Color(0xFF34C759)

val GaoDeWhite = MetroWhite
val GaoDeCard = MetroCard
val GaoDeTextPrimary = MetroTextPrimary
val GaoDeTextSecondary = MetroTextSecondary
val GaoDeTransfer = MetroTransfer
val GaoDeGreen = MetroGreen

val GaoDeColorScheme = lightColorScheme(
    primary = MetroBlue,
    onPrimary = Color.White,
    primaryContainer = MetroBlueSoft,
    onPrimaryContainer = MetroBlue,
    secondary = Color(0xFF00A884),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAF8F2),
    onSecondaryContainer = Color(0xFF006B55),
    background = MetroWhite,
    onBackground = MetroTextPrimary,
    surface = Color.White,
    onSurface = MetroTextPrimary,
    surfaceVariant = MetroCard,
    onSurfaceVariant = MetroTextSecondary,
    outline = Color(0xFFE5E5EA),
    outlineVariant = Color(0xFFE5E5EA),
    error = MetroTransfer,
    onError = Color.White,
)

val MetroDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DB3FF),
    onPrimary = Color(0xFF003A7A),
    primaryContainer = Color(0xFF0C3D7A),
    onPrimaryContainer = Color(0xFFD6E8FF),
    secondary = Color(0xFF77D9BC),
    onSecondary = Color(0xFF00382D),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF171C22),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF222A33),
    onSurfaceVariant = Color(0xFFB8C0CC),
    outline = Color(0xFF3B4652),
    outlineVariant = Color(0xFF303944),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)
