package com.zilagent.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MorphingBackground(
    colorPalette: Pair<Color, Color>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "morphing")
    
    // Suggestion 5: More complex time-based movement
    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val color1 by animateColorAsState(targetValue = colorPalette.first, animationSpec = tween(3000), label = "c1")
    val color2 by animateColorAsState(targetValue = colorPalette.second, animationSpec = tween(3000), label = "c2")

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dynamic offsets for 4 blobs (increased complexity for "Dynamic Backgrounds" suggestion)
        val blob1Center = Offset(
            x = width * (0.5f + 0.35f * cos(t + 0.5f)),
            y = height * (0.5f + 0.25f * sin(t * 0.7f))
        )
        val blob2Center = Offset(
            x = width * (0.2f + 0.3f * sin(t * 1.1f)),
            y = height * (0.8f + 0.25f * cos(t * 0.4f))
        )
        val blob3Center = Offset(
            x = width * (0.85f + 0.15f * cos(t * 1.8f)),
            y = height * (0.25f + 0.45f * sin(t * 0.9f))
        )
        val blob4Center = Offset(
            x = width * (0.4f + 0.4f * sin(t * 0.6f)),
            y = height * (0.1f + 0.3f * cos(t * 1.3f))
        )

        // Draw base gradient with a mix color
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color1.copy(alpha = 0.9f),
                    color2.copy(alpha = 0.9f)
                )
            )
        )

        // Blobs with slightly varied radius
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = 0.45f), Color.Transparent),
                center = blob1Center,
                radius = width * (0.8f + 0.1f * sin(t))
            ),
            center = blob1Center,
            radius = width * (0.8f + 0.1f * sin(t))
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2.copy(alpha = 0.4f), Color.Transparent),
                center = blob2Center,
                radius = width * (0.7f + 0.1f * cos(t * 0.5f))
            ),
            center = blob2Center,
            radius = width * (0.7f + 0.1f * cos(t * 0.5f))
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = 0.35f), Color.Transparent),
                center = blob3Center,
                radius = width * 0.65f
            ),
            center = blob3Center,
            radius = width * 0.65f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2.copy(alpha = 0.3f), Color.Transparent),
                center = blob4Center,
                radius = width * 0.75f
            ),
            center = blob4Center,
            radius = width * 0.75f
        )
    }
}
