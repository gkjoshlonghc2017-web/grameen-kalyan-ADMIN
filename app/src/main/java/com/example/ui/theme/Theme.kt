package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = GkGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GkGreenContainer,
    onPrimaryContainer = GkOnGreenContainer,
    secondary = GkGoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = GkGoldContainer,
    onSecondaryContainer = GkOnGoldContainer,
    tertiary = GkTealAccent,
    onTertiary = Color.White,
    tertiaryContainer = GkTealContainer,
    background = GkBackground,
    onBackground = GkTextPrimary,
    surface = GkSurface,
    onSurface = GkTextPrimary,
    surfaceVariant = GkSurfaceVariant,
    onSurfaceVariant = GkTextSecondary,
    outline = GkOutline,
    outlineVariant = GkOutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF48C78E),
    onPrimary = Color(0xFF003820),
    primaryContainer = Color(0xFF064E30),
    onPrimaryContainer = Color(0xFFA6F5C7),
    secondary = Color(0xFFE5C067),
    onSecondary = Color(0xFF402D00),
    secondaryContainer = Color(0xFF5C4300),
    onSecondaryContainer = Color(0xFFFFDF94),
    tertiary = Color(0xFF64D2E6),
    background = Color(0xFF111713),
    onBackground = Color(0xFFE1E4E0),
    surface = Color(0xFF161F1A),
    onSurface = Color(0xFFE1E4E0),
    surfaceVariant = Color(0xFF26332B),
    onSurfaceVariant = Color(0xFFC3CCC5)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep official healthcare green and gold palette
    content: @Composable () -> Unit,
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
