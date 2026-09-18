package com.example.ui.theme

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
import android.app.Activity

private val DarkColorScheme = darkColorScheme(
    primary = SpendoraPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = SpendoraDarkSurfaceElevated,
    onPrimaryContainer = SpendoraPurpleLight,
    secondary = SpendoraPurpleLight,
    onSecondary = Color.Black,
    secondaryContainer = SpendoraDarkBorder,
    onSecondaryContainer = Color.White,
    tertiary = SpendoraSavingsPurple,
    onTertiary = Color.White,
    background = SpendoraDarkBackground,
    onBackground = SpendoraTextPrimaryDark,
    surface = SpendoraDarkSurface,
    onSurface = SpendoraTextPrimaryDark,
    surfaceVariant = SpendoraDarkSurfaceElevated,
    onSurfaceVariant = SpendoraTextSecondaryDark,
    outline = SpendoraDarkBorder,
    outlineVariant = SpendoraDarkBorderSubtle,
    error = SpendoraExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SpendoraPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = SpendoraPurpleDark,
    secondary = SpendoraPurpleDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = SpendoraTextPrimaryLight,
    tertiary = SpendoraSavingsPurple,
    onTertiary = Color.White,
    background = SpendoraLightBackground,
    onBackground = SpendoraTextPrimaryLight,
    surface = SpendoraLightSurface,
    onSurface = SpendoraTextPrimaryLight,
    surfaceVariant = SpendoraLightSurfaceElevated,
    onSurfaceVariant = SpendoraTextSecondaryLight,
    outline = SpendoraLightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = SpendoraExpenseRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Ensure status bar is transparent so content draws edge-to-edge behind it
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                val insetsController = WindowCompat.getInsetsController(window, view)
                // When dark theme: isAppearanceLightStatusBars = false (white icons)
                // When light theme: isAppearanceLightStatusBars = true (dark icons)
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
