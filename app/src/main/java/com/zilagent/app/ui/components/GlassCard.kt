package com.zilagent.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val bgTop = if (isLightTheme) Color.White.copy(alpha = 0.82f) else Color.Black.copy(alpha = 0.55f)
    val bgBottom = if (isLightTheme) Color.White.copy(alpha = 0.68f) else Color.Black.copy(alpha = 0.35f)
    val borderTop = if (isLightTheme) Color(0xFF6B7280).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.30f)
    val borderBottom = if (isLightTheme) Color(0xFF9CA3AF).copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgTop, bgBottom)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(borderTop, borderBottom)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            // Note: Real blur requires RenderEffect (API 31+) or a library.
            // For now, we rely on the semi-transparent overlay to give the "feel".
            // If we had a rich background underneath, this would look "glassy".
            ,
        content = content
    )
}
