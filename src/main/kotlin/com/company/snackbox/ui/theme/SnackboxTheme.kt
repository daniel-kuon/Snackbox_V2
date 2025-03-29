package com.company.snackbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme colors
private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF004BA0),
    secondary = Color(0xFF689F38),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEDC8),
    onSecondaryContainer = Color(0xFF33691E),
    tertiary = Color(0xFFFF9800),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFFE65100),
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFDEDED),
    onErrorContainer = Color(0xFF5F1412),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212)
)

// Dark theme colors
private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFFAED581),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF33691E),
    onSecondaryContainer = Color(0xFFDCEDC8),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFE65100),
    onTertiaryContainer = Color(0xFFFFE0B2),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFF5F1412),
    onErrorContainer = Color(0xFFFDEDED),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0)
)

/**
 * Snackbox theme for the application
 *
 * @param darkTheme Whether to use dark theme
 * @param content Content to be themed
 */
@Composable
fun SnackboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
