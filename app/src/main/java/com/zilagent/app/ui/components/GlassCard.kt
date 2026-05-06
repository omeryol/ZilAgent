package com.zilagent.app.ui.components

import androidx.compose.foundation.background
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
    val bgTop = if (isLightTheme) Color.White.copy(alpha = 0.86f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f)
    val bgBottom = if (isLightTheme) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.54f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgTop, bgBottom)
                )
            )
            ,
        content = content
    )
}
