package com.zilagent.app.ui.components

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    var themeColorName by remember { mutableStateOf(WidgetStore.getThemeColorName(context)) }
    var themeMode by remember { mutableStateOf(WidgetStore.getThemeMode(context)) }
    var backgroundMode by remember { mutableStateOf(WidgetStore.getAppBackgroundMode(context)) }

    DisposableEffect(context) {
        val prefs = context.getSharedPreferences(WidgetStore.PREFS_NAME, Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            when (key) {
                "THEME_COLOR" -> themeColorName = p.getString(key, "Lavanta") ?: "Lavanta"
                "THEME_MODE" -> themeMode = p.getInt(key, 0)
                "APP_BACKGROUND_MODE" -> backgroundMode = p.getInt(key, 0).coerceIn(0, 4)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

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
        if (backgroundMode == 0 || backgroundMode == 1 || backgroundMode == 3) {
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

            Box(
                modifier = Modifier
                    .offset(x = (-50).dp, y = (-50).dp)
                    .offset(y = offset1.dp)
                    .size(300.dp)
                    .blur(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isDark) color1.copy(alpha = 0.4f) else color1.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

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
                                if (isDark) color2.copy(alpha = 0.4f) else color2.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-100).dp)
                    .size(200.dp)
                    .blur(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isDark) color2.copy(alpha = 0.2f) else color1.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        if (backgroundMode == 1) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 34.dp.toPx()
                val lineColor = if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f)
                val tileColor = if (isDark) Color.White.copy(alpha = 0.035f) else Color.White.copy(alpha = 0.18f)
                var y = 0f
                var row = 0
                while (y < size.height + step) {
                    var x = 0f
                    var col = 0
                    while (x < size.width + step) {
                        if ((row + col) % 2 == 0) {
                            drawRect(
                                color = tileColor,
                                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(step, step)
                            )
                        }
                        x += step
                        col++
                    }
                    y += step
                    row++
                }
                var lx = 0f
                while (lx < size.width + step) {
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(lx, 0f), androidx.compose.ui.geometry.Offset(lx, size.height), strokeWidth = 1f)
                    lx += step
                }
                var ly = 0f
                while (ly < size.height + step) {
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, ly), androidx.compose.ui.geometry.Offset(size.width, ly), strokeWidth = 1f)
                    ly += step
                }
            }
        }

        if (backgroundMode == 3) {
            val aurora = rememberInfiniteTransition(label = "aurora")
            val shift by aurora.animateFloat(
                initialValue = -220f,
                targetValue = 220f,
                animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
                label = "auroraShift",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                color1.copy(alpha = if (isDark) 0.18f else 0.22f),
                                color2.copy(alpha = if (isDark) 0.20f else 0.24f),
                                Color.Transparent,
                            ),
                            start = androidx.compose.ui.geometry.Offset(shift, 0f),
                            end = androidx.compose.ui.geometry.Offset(1200f + shift, 1800f),
                        ),
                    ),
            )
        }

        if (backgroundMode == 4) {
            val dots = rememberInfiniteTransition(label = "dots")
            val drift by dots.animateFloat(
                initialValue = 0f,
                targetValue = 28f,
                animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
                label = "dotsDrift",
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spacing = 28.dp.toPx()
                val dotColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                val lineColor = if (isDark) color2.copy(alpha = 0.10f) else color1.copy(alpha = 0.10f)

                var y = -spacing
                while (y < size.height + spacing) {
                    var x = -spacing
                    while (x < size.width + spacing) {
                        drawCircle(
                            color = dotColor,
                            radius = 1.8.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x + drift, y - drift),
                        )
                        x += spacing
                    }
                    y += spacing
                }

                var lx = -size.height
                while (lx < size.width + size.height) {
                    drawLine(
                        color = lineColor,
                        start = androidx.compose.ui.geometry.Offset(lx + drift, 0f),
                        end = androidx.compose.ui.geometry.Offset(lx + size.height + drift, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                    lx += spacing * 3
                }
            }
        }

        content()
    }
}
