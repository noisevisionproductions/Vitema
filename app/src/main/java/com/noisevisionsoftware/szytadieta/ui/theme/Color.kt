package com.noisevisionsoftware.szytadieta.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Główne kolory zainspirowane logo
val BrightGreen = Color(0xFF7CB342)
val DarkGreen = Color(0xFF2E7D32)
val White = Color(0xFFFFFFFF)

val Amber80 = Color(0xFF8C6D1F)
val Amber40 = Color(0xFFD5BF30)

val Green90 = Color(0xFF1B5E20)
val Green70 = Color(0xFF43A047)
val Green30 = Color(0xFF81C784)
val Green20 = Color(0xFFA5D6A7)

val Grey90 = Color(0xFF202020)
val Grey80 = Color(0xFF404040)
val Grey70 = Color(0xFF606060)
val Grey40 = Color(0xFF999999)
val Grey20 = Color(0xFFE0E0E0)
val Grey10 = Color(0xFFF5F5F5)

val DarkColorScheme = darkColorScheme(
    primary = BrightGreen,
    onPrimary = Grey90,
    primaryContainer = DarkGreen,
    onPrimaryContainer = Green20,

    secondary = Amber80,
    onSecondary = White,
    secondaryContainer = Amber40,
    onSecondaryContainer = Grey90,

    tertiary = Green70,
    onTertiary = White,
    tertiaryContainer = Green30,
    onTertiaryContainer = Grey90,

    background = Color.Transparent,
    onBackground = White,

    surface = Grey80,
    onSurface = White,
    surfaceVariant = Grey70,
    onSurfaceVariant = Grey20,

    outline = Grey40,
    outlineVariant = Grey70,

    error = Color(0xFFCF6679),
    onError = Grey90,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = White,

    inversePrimary = Green30,
    inverseSurface = Grey20,
    inverseOnSurface = Grey90,

    scrim = Color(0xFF000000)
)

val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = White,
    primaryContainer = BrightGreen,
    onPrimaryContainer = Grey90,

    secondary = Amber40,
    onSecondary = Grey90,
    secondaryContainer = Amber80,
    onSecondaryContainer = White,

    tertiary = Green70,
    onTertiary = White,
    tertiaryContainer = Green20,
    onTertiaryContainer = Green90,

    background = Color.Transparent,
    onBackground = Grey90,

    surface = Grey10,
    onSurface = Grey90,
    surfaceVariant = Grey20,
    onSurfaceVariant = Grey80,

    outline = Grey40,
    outlineVariant = Grey20,

    error = Color(0xFFB00020),
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    inversePrimary = Green70,
    inverseSurface = Grey80,
    inverseOnSurface = Grey20,

    scrim = Color(0xFF000000)
)