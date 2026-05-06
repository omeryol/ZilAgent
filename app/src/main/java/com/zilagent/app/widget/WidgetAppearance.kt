package com.zilagent.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.RemoteViews
import com.zilagent.app.R
import kotlin.math.roundToInt

data class WidgetPalette(
    val text: Int,
    val mutedText: Int,
    val accent: Int,
    val success: Int,
    val warning: Int,
    val chipText: Int,
    val chipBackground: Int,
    val divider: Int,
    val footer: Int,
)

object WidgetAppearance {
    private val backgroundCache = mutableMapOf<String, Bitmap>()

    private data class WidgetBackgroundSpec(
        val colors: IntArray,
        val stroke: Int,
    )

    fun palette(preset: WidgetVisualPreset): WidgetPalette {
        return when (preset) {
            WidgetVisualPreset.Slate -> WidgetPalette(
                text = Color.parseColor("#F7F8FA"),
                mutedText = Color.parseColor("#B9C1D1"),
                accent = Color.parseColor("#7DC4FF"),
                success = Color.parseColor("#8DE0C1"),
                warning = Color.parseColor("#FFD166"),
                chipText = Color.parseColor("#DDF4FF"),
                chipBackground = Color.parseColor("#1F3146"),
                divider = Color.parseColor("#2AFFFFFF"),
                footer = Color.parseColor("#E8F2FF"),
            )

            WidgetVisualPreset.Paper -> WidgetPalette(
                text = Color.parseColor("#18202A"),
                mutedText = Color.parseColor("#617085"),
                accent = Color.parseColor("#2563EB"),
                success = Color.parseColor("#137A4B"),
                warning = Color.parseColor("#B7791F"),
                chipText = Color.parseColor("#204276"),
                chipBackground = Color.parseColor("#DDE9FF"),
                divider = Color.parseColor("#1A203040"),
                footer = Color.parseColor("#243042"),
            )

            WidgetVisualPreset.Dawn -> WidgetPalette(
                text = Color.parseColor("#FFF8F4"),
                mutedText = Color.parseColor("#F3D8D0"),
                accent = Color.parseColor("#FF9F6E"),
                success = Color.parseColor("#FFD670"),
                warning = Color.parseColor("#FFF3B0"),
                chipText = Color.parseColor("#FFF4EA"),
                chipBackground = Color.parseColor("#8B4F45"),
                divider = Color.parseColor("#33FFFFFF"),
                footer = Color.parseColor("#FFF3EC"),
            )

            WidgetVisualPreset.Grove -> WidgetPalette(
                text = Color.parseColor("#F3FFF7"),
                mutedText = Color.parseColor("#B9DDC8"),
                accent = Color.parseColor("#6EE7B7"),
                success = Color.parseColor("#A7F3D0"),
                warning = Color.parseColor("#FDE68A"),
                chipText = Color.parseColor("#E8FFF2"),
                chipBackground = Color.parseColor("#20463D"),
                divider = Color.parseColor("#26FFFFFF"),
                footer = Color.parseColor("#E9FFF3"),
            )

            WidgetVisualPreset.Neon -> WidgetPalette(
                text = Color.parseColor("#F7F4FF"),
                mutedText = Color.parseColor("#C9BFFF"),
                accent = Color.parseColor("#8E7CFF"),
                success = Color.parseColor("#55E1FF"),
                warning = Color.parseColor("#FFD166"),
                chipText = Color.parseColor("#F4ECFF"),
                chipBackground = Color.parseColor("#3B2470"),
                divider = Color.parseColor("#33FFFFFF"),
                footer = Color.parseColor("#EEE7FF"),
            )

            WidgetVisualPreset.Lagoon -> WidgetPalette(
                text = Color.parseColor("#EFFBFF"),
                mutedText = Color.parseColor("#A6D9E6"),
                accent = Color.parseColor("#4FD1C5"),
                success = Color.parseColor("#9AE6B4"),
                warning = Color.parseColor("#F6E05E"),
                chipText = Color.parseColor("#E8FFFF"),
                chipBackground = Color.parseColor("#114B5F"),
                divider = Color.parseColor("#26FFFFFF"),
                footer = Color.parseColor("#DFF9FF"),
            )

            WidgetVisualPreset.Ember -> WidgetPalette(
                text = Color.parseColor("#FFF7F2"),
                mutedText = Color.parseColor("#F0C7B8"),
                accent = Color.parseColor("#FF7A59"),
                success = Color.parseColor("#FFD166"),
                warning = Color.parseColor("#FFE7A3"),
                chipText = Color.parseColor("#FFF3EB"),
                chipBackground = Color.parseColor("#6A2C1B"),
                divider = Color.parseColor("#33FFFFFF"),
                footer = Color.parseColor("#FFF0E8"),
            )

            WidgetVisualPreset.Mono -> WidgetPalette(
                text = Color.parseColor("#F6F7F8"),
                mutedText = Color.parseColor("#C9CDD2"),
                accent = Color.parseColor("#F0F3F6"),
                success = Color.parseColor("#DDE2E7"),
                warning = Color.parseColor("#FFFFFF"),
                chipText = Color.parseColor("#F8FAFC"),
                chipBackground = Color.parseColor("#23272D"),
                divider = Color.parseColor("#33FFFFFF"),
                footer = Color.parseColor("#E9EDF1"),
            )
        }
    }

    fun applyBackground(context: Context, views: RemoteViews, preset: WidgetVisualPreset) {
        views.setImageViewBitmap(
            R.id.widget_background,
            backgroundBitmap(
                context = context,
                preset = preset,
                cornerRadiusDp = WidgetStore.getWidgetCornerRadius(context),
            ),
        )
    }

    private fun backgroundBitmap(
        context: Context,
        preset: WidgetVisualPreset,
        cornerRadiusDp: Int,
    ): Bitmap {
        val safeRadius = cornerRadiusDp.coerceIn(0, 36)
        val cacheKey = "${preset.key}:$safeRadius"
        backgroundCache[cacheKey]?.let { cached -> return cached }

        val width = 1200
        val height = 720
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val density = context.resources.displayMetrics.density
        val spec = backgroundSpec(preset)
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            spec.colors,
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = safeRadius * density
            setStroke(density.roundToInt().coerceAtLeast(1), spec.stroke)
        }
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        backgroundCache[cacheKey] = bitmap
        return bitmap
    }

    private fun backgroundSpec(preset: WidgetVisualPreset): WidgetBackgroundSpec {
        return when (preset) {
            WidgetVisualPreset.Slate -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#DD101923"),
                    Color.parseColor("#CC24384E"),
                ),
                stroke = Color.parseColor("#2EFFFFFF"),
            )

            WidgetVisualPreset.Paper -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#FFFDF8"),
                    Color.parseColor("#F3F7FF"),
                ),
                stroke = Color.parseColor("#14243E66"),
            )

            WidgetVisualPreset.Dawn -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#DDB86B5C"),
                    Color.parseColor("#CC8B4F45"),
                ),
                stroke = Color.parseColor("#2EFFF1E8"),
            )

            WidgetVisualPreset.Grove -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#DD0E241E"),
                    Color.parseColor("#CC22423B"),
                ),
                stroke = Color.parseColor("#29E6FFF1"),
            )

            WidgetVisualPreset.Neon -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#DD151125"),
                    Color.parseColor("#CC37205D"),
                ),
                stroke = Color.parseColor("#33F7F1FF"),
            )

            WidgetVisualPreset.Lagoon -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#DD0D2B3A"),
                    Color.parseColor("#CC1C7C8C"),
                ),
                stroke = Color.parseColor("#2EFFFFFF"),
            )

            WidgetVisualPreset.Ember -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#DD2C0F0A"),
                    Color.parseColor("#CC8C4025"),
                ),
                stroke = Color.parseColor("#33FFFFFF"),
            )

            WidgetVisualPreset.Mono -> WidgetBackgroundSpec(
                colors = intArrayOf(
                    Color.parseColor("#DD111315"),
                    Color.parseColor("#CC3A4047"),
                ),
                stroke = Color.parseColor("#26FFFFFF"),
            )
        }
    }

    fun countdownAccent(
        palette: WidgetPalette,
        dynamicAccent: Boolean,
        secondsRemaining: Long,
    ): Int {
        if (!dynamicAccent || secondsRemaining <= 0) return palette.accent
        return when {
            secondsRemaining <= 5 * 60 -> palette.warning
            secondsRemaining <= 15 * 60 -> alpha(palette.warning, 0.88f)
            else -> palette.success
        }
    }

    fun alpha(color: Int, factor: Float): Int {
        val alpha = (factor.coerceIn(0f, 1f) * 255f).roundToInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    fun scale(base: Float, scale: WidgetElementScale): Float {
        return base * scale.textMultiplier
    }
}
