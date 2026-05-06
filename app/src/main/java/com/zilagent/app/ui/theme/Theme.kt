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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun ZilAgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPaletteName: String = "Lavanta",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val palette = ThemePalette.getPalette(colorPaletteName)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = palette.first,
            onPrimary = Color(0xFF001C8E),
            secondary = PrismLesson,
            onSecondary = Color(0xFF062113),
            tertiary = PrismBreak,
            onTertiary = Color(0xFF2B2908),
            error = PrismAlert,
            onError = Color.White,
            background = PrismSurface,
            onBackground = PrismOnDark,
            surface = PrismSurface,
            onSurface = PrismOnDark,
            surfaceVariant = PrismSurfaceVariant,
            onSurfaceVariant = PrismOnDarkMuted,
            surfaceTint = palette.first,
            outlineVariant = PrismOutlineVariant,
            primaryContainer = PrismSurfaceContainerHigh,
            onPrimaryContainer = PrismOnDark,
            secondaryContainer = PrismLessonDim.copy(alpha = 0.22f),
            tertiaryContainer = PrismBreakDim.copy(alpha = 0.22f),
            errorContainer = PrismAlertDim.copy(alpha = 0.22f),
            inverseSurface = Color(0xFFF4F6FA),
            inverseOnSurface = PrismSurface
        )
        else -> lightColorScheme(
            primary = palette.first,
            onPrimary = Color.White,
            secondary = Color(0xFF1A8F52),
            onSecondary = Color.White,
            tertiary = Color(0xFF9A8600),
            onTertiary = Color.White,
            error = Color(0xFFC5294F),
            onError = Color.White,
            background = Color(0xFFF6F7FB),
            onBackground = Color(0xFF12141A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF12141A),
            surfaceVariant = Color(0xFFF0F2F7),
            onSurfaceVariant = Color(0xFF465066),
            surfaceTint = palette.first,
            outlineVariant = Color(0x33465066),
            primaryContainer = Color(0xFFE9EEFF),
            onPrimaryContainer = Color(0xFF1D2751)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
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

