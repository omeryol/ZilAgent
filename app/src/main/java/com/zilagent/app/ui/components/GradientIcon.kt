package com.zilagent.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GradientIcon(
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp,
    contentDescription: String? = null,
    isSquare: Boolean = false
) {
    val shape = if (isSquare) RoundedCornerShape(12.dp) else CircleShape
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        // Inner Glow / Glass effect overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = 0.15f))
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

object IconGradients {
    val Blue = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
    val Purple = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
    val Pink = listOf(Color(0xFFF093FB), Color(0xFFF5576C))
    val Orange = listOf(Color(0xFFFAD0C4), Color(0xFFFFD1FF)) // Soft version
    val VibrantOrange = listOf(Color(0xFFFFA647), Color(0xFFFF6B6B))
    val Green = listOf(Color(0xFF43E97B), Color(0xFF38F9D7))
    val Sunset = listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))
    val Lava = listOf(Color(0xFFF83600), Color(0xFFF9D423))
}
