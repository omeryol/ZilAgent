package com.zilagent.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zilagent.app.ui.theme.ThemePalette
import com.zilagent.app.widget.WidgetStore

@Composable
fun ZilAgentBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    
    val themeColorName = WidgetStore.getThemeColorName(context)
    val themeMode = WidgetStore.getThemeMode(context)

    val isDark = when(themeMode) {
        1 -> false
        2 -> true
        else -> isSystemDark
    }

    val palette = ThemePalette.getPalette(themeColorName)
    val color1 = palette.first
    val color2 = palette.second
    
    // Background Base Color
    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), 
                Color(0xFF020617)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8FAFC),
                Color(0xFFF1F5F9)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // Aesthetic Blobs (Glassmorphism)
        // We use infinite transitions for subtle movement
        val infiniteTransition = rememberInfiniteTransition(label = "blobs")
        
        val offset1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 20f,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
            label = "blob1"
        )
        
        val offset2 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = -30f,
            animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing), RepeatMode.Reverse),
            label = "blob2"
        )
        
        // Top Left Blob
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50).dp)
                .offset(y = offset1.dp)
                .size(300.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if(isDark) color1.copy(alpha = 0.4f) else color1.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Bottom Right Blob
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .offset(y = offset2.dp)
                .size(350.dp)
                .blur(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if(isDark) color2.copy(alpha = 0.4f) else color2.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Center/Random Blob
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-100).dp)
                .size(200.dp)
                .blur(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if(isDark) color2.copy(alpha = 0.2f) else color1.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        content()
    }
}
