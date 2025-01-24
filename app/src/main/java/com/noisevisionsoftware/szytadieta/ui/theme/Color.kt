package com.noisevisionsoftware.szytadieta.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val BrightGreen = Color(0xFF7CB342)
val DarkGreen = Color(0xFF2E7D32)
val White = Color(0xFFFFFFFF)

val LightTeal = Color(0xFF80CBC4)

val Amber80 = Color(0xFF8C6D1F)
val Amber40 = Color(0xFFD5BF30)

val Green90 = Color(0xFF1B5E20)
val Green70 = Color(0xFF43A047)
val Green30 = Color(0xFF81C784)
val Green20 = Color(0xFFA5D6A7)
val Green10 = Color(0xFFDCEDC8)

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
    primary = BrightGreen, // Retained for consistency
    onPrimary = Grey10, // High contrast on light backgrounds
    primaryContainer = Green10, // Softer green for containers
    onPrimaryContainer = DarkGreen, // Contrast against container

    secondary = Color(0xFF00796B), // Teal shade replacing Amber
    onSecondary = Grey10, // High contrast text on teal
    secondaryContainer = LightTeal, // Lighter teal for containers
    onSecondaryContainer = White, // High contrast text on container

    tertiary = Green30, // Softer tertiary shade
    onTertiary = Grey10, // High contrast
    tertiaryContainer = Green10, // Softer container green
    onTertiaryContainer = Green90, // Darker contrast for visibility

    background = Color.Transparent, // Light base background
    onBackground = Grey90, // Good contrast with background

    surface = Grey20, // Slightly darker surface than background
    onSurface = Grey90, // High contrast text on surfaces
    surfaceVariant = Grey10, // Variant slightly darker than surface
    onSurfaceVariant = Grey80, // Maintains visual hierarchy

    outline = Grey40, // Matches dark scheme outline
    outlineVariant = Grey20, // Softer variant for light mode

    error = Color(0xFFB00020), // Same red for errors
    onError = White, // High contrast for readability
    errorContainer = Color(0xFFFFDAD6), // Softer error container
    onErrorContainer = Color(0xFF410002), // Matches dark scheme

    inversePrimary = Green30, // Light inverse of primary
    inverseSurface = Grey80, // Darker contrast for inversions
    inverseOnSurface = Grey10, // Light text on inverse surface

    scrim = Color(0xFF000000) // Unchanged
)
