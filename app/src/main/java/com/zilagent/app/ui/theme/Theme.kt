package com.zilagent.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ZilAgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorPaletteName = androidx.compose.runtime.remember { com.zilagent.app.widget.WidgetStore.getThemeColorName(context) }
    
    // Define preset color schemes
    val colorPalette = when (colorPaletteName) {
        "Okyanus" -> Pair(androidx.compose.ui.graphics.Color(0xFF4FACFE), androidx.compose.ui.graphics.Color(0xFF00F2FE))
        "Orman" -> Pair(androidx.compose.ui.graphics.Color(0xFF43E97B), androidx.compose.ui.graphics.Color(0xFF38F9D7))
        "Gece" -> Pair(androidx.compose.ui.graphics.Color(0xFF243B55), androidx.compose.ui.graphics.Color(0xFF141E30))
        "Ateş" -> Pair(androidx.compose.ui.graphics.Color(0xFFF093FB), androidx.compose.ui.graphics.Color(0xFFF5576C))
        "Güneş" -> Pair(androidx.compose.ui.graphics.Color(0xFFF6D365), androidx.compose.ui.graphics.Color(0xFFFDA085))
        else -> Pair(androidx.compose.ui.graphics.Color(0xFFE0C3FC), androidx.compose.ui.graphics.Color(0xFF8EC5FC)) // Lavanta (Default)
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = colorPalette.first,
            secondary = colorPalette.second,
            tertiary = Pink80
        )
        else -> lightColorScheme(
            primary = colorPalette.first,
            secondary = colorPalette.second,
            tertiary = Pink40
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
