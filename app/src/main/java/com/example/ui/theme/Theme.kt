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

private val DarkColorScheme = darkColorScheme(
    primary = WinePrimaryDark,
    onPrimary = Color(0xFF410006),
    primaryContainer = WineContainerDark,
    onPrimaryContainer = Color(0xFFFFDAD8),
    secondary = AmberSecondaryDark,
    onSecondary = Color(0xFF492800),
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = Color(0xFFFFDDB8),
    tertiary = EmeraldTertiaryDark,
    onTertiary = Color(0xFF00391C),
    tertiaryContainer = EmeraldContainerDark,
    onTertiaryContainer = Color(0xFFB5F2C5),
    background = DarkParchment,
    onBackground = Color(0xFFECE0DB),
    surface = DarkSurface,
    onSurface = Color(0xFFECE0DB),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD7C2BC),
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = WinePrimary,
    onPrimary = Color.White,
    primaryContainer = WineContainer,
    onPrimaryContainer = Color(0xFF410006),
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = EmeraldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = Color(0xFF00210E),
    background = ParchmentLight,
    onBackground = Color(0xFF221A17),
    surface = SurfaceLight,
    onSurface = Color(0xFF221A17),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF52433E),
    outline = OutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
