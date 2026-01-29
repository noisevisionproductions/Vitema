package com.noisevisionsoftware.vitema.ui.theme

import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalPatternColor = compositionLocalOf { Color.Unspecified }
val LocalBackgroundColor = compositionLocalOf { Color.Unspecified }

@Composable
fun FitApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val patternColor = if (darkTheme) {
        colorScheme.primary.copy(alpha = 0.4f)
    } else {
        colorScheme.primary.copy(alpha = 0.7f)
    }

    val backgroundColor = if (darkTheme) {
        Grey90
    } else {
        Grey10
    }

    CompositionLocalProvider(
        LocalPatternColor provides patternColor,
        LocalBackgroundColor provides backgroundColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}