package com.sunguard.vault.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = BlackObsidian,
    primaryContainer = GoldDark,
    onPrimaryContainer = SandyLight,
    
    secondary = Turquoise,
    onSecondary = BlackObsidian,
    secondaryContainer = TurquoiseDark,
    onSecondaryContainer = SandyLight,
    
    tertiary = Lazurite,
    onTertiary = SandyLight,
    
    background = BlackObsidian,
    onBackground = SandyLight,
    
    surface = ObsidianLight,
    onSurface = SandyLight,
    surfaceVariant = Color(0xFF252525),
    onSurfaceVariant = Color(0xFFCCCCCC),
    
    error = ErrorRed,
    onError = BlackObsidian,
    
    outline = Gold.copy(alpha = 0.3f),
    outlineVariant = Gold.copy(alpha = 0.15f)
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = SandyLight,
    onPrimaryContainer = BlackObsidian,
    
    secondary = TurquoiseDark,
    onSecondary = Color.White,
    secondaryContainer = Turquoise.copy(alpha = 0.2f),
    onSecondaryContainer = BlackObsidian,
    
    tertiary = Lazurite,
    onTertiary = Color.White,
    
    background = SandyLight,
    onBackground = BlackObsidian,
    
    surface = Color.White,
    onSurface = BlackObsidian,
    surfaceVariant = SandyDark,
    onSurfaceVariant = Color(0xFF444444),
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = GoldDark.copy(alpha = 0.5f),
    outlineVariant = GoldDark.copy(alpha = 0.2f)
)

@Composable
fun SunGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    // Status bar and navigation bar colors are handled by enableEdgeToEdge()
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

