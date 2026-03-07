package com.zilagent.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer

@Stable
data class PremiumMotionConfig(
    val enabled: Boolean = true,
    val intensity: Int = 60,
    val style: Int = 0, // 0: Yaylı, 1: Sıçrama, 2: Akıcı
)

val LocalPremiumMotionConfig = staticCompositionLocalOf { PremiumMotionConfig() }

private fun animationSpecFor(style: Int, intensity: Int): FiniteAnimationSpec<Float> {
    val p = intensity.coerceIn(10, 100) / 100f
    return when (style) {
        1 -> spring(dampingRatio = 0.56f - (0.12f * p), stiffness = 640f - (220f * p))
        2 -> tween(durationMillis = (130 - (50 * p)).toInt().coerceIn(80, 130), easing = FastOutSlowInEasing)
        else -> spring(dampingRatio = 0.72f - (0.10f * p), stiffness = 780f - (280f * p))
    }
}

fun Modifier.premiumTouchEffect(): Modifier = composed {
    val config = LocalPremiumMotionConfig.current
    var pressed by remember { mutableStateOf(false) }
    val strength = (config.intensity.coerceIn(10, 100) / 100f)
    val minScale = (1f - (0.02f + 0.03f * strength)).coerceAtLeast(0.92f)
    val scale by animateFloatAsState(
        targetValue = if (config.enabled && pressed) minScale else 1f,
        animationSpec = animationSpecFor(config.style, config.intensity),
        label = "premium_touch_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (config.enabled && pressed) 0.94f else 1f,
        animationSpec = animationSpecFor(config.style, config.intensity),
        label = "premium_touch_alpha",
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .pointerInput(config.enabled) {
            if (!config.enabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}

fun Modifier.premiumClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    this
        .premiumTouchEffect()
        .clickable(enabled = enabled, onClick = onClick)
}
