package com.zilagent.app.widget

enum class WidgetVisualPreset(val key: String) {
    Slate("slate"),
    Paper("paper"),
    Dawn("dawn"),
    Grove("grove"),
    Neon("neon"),
    Lagoon("lagoon"),
    Ember("ember"),
    Mono("mono");

    companion object {
        fun fromKey(raw: String?): WidgetVisualPreset {
            return values().firstOrNull { it.key == raw } ?: Slate
        }
    }
}

enum class WidgetStyleFamily(val key: String) {
    Minimal("minimal"),
    Academic("academic"),
    Agenda("agenda"),
    Energetic("energetic"),
    Night("night"),
    Mono("mono");

    companion object {
        fun fromKey(raw: String?): WidgetStyleFamily {
            return values().firstOrNull { it.key == raw } ?: Academic
        }
    }
}

enum class WidgetInfoDensity(val key: String) {
    Sparse("sparse"),
    Balanced("balanced"),
    Dense("dense");

    companion object {
        fun fromKey(raw: String?): WidgetInfoDensity {
            return values().firstOrNull { it.key == raw } ?: Balanced
        }
    }
}

enum class WidgetTypographyPreset(val key: String) {
    Soft("soft"),
    Strong("strong"),
    Technical("technical"),
    Notebook("notebook");

    companion object {
        fun fromKey(raw: String?): WidgetTypographyPreset {
            return values().firstOrNull { it.key == raw } ?: Strong
        }
    }
}

enum class WidgetElementScale(val key: String, val textMultiplier: Float) {
    Small("small", 0.84f),
    Medium("medium", 1f),
    Large("large", 1.18f);

    companion object {
        fun fromKey(raw: String?): WidgetElementScale {
            return values().firstOrNull { it.key == raw } ?: Medium
        }
    }
}

data class WidgetElementPreferences(
    val visible: Boolean,
    val position: Int,
    val scale: WidgetElementScale,
)

enum class CountdownWidgetElement(
    val key: String,
    val defaultPosition: Int,
    val defaultScale: WidgetElementScale,
    val defaultVisible: Boolean,
    val defaultSize: Int,
) {
    Badge("badge", 0, WidgetElementScale.Small, true, 14),
    Meta("meta", 1, WidgetElementScale.Small, true, 16),
    Countdown("countdown", 2, WidgetElementScale.Large, true, 30),
    Title("title", 3, WidgetElementScale.Medium, true, 22),
    Current("current", 4, WidgetElementScale.Medium, true, 18),
    Next("next", 5, WidgetElementScale.Medium, true, 18),
    Progress("progress", 1, WidgetElementScale.Medium, true, 16);

    companion object {
        fun fromKey(raw: String): CountdownWidgetElement? = values().firstOrNull { it.key == raw }
    }
}

enum class SyllabusActiveHighlightStyle(val key: String) {
    Bold("bold"),
    Accent("accent"),
    Soft("soft"),
    Strong("strong");

    companion object {
        fun fromKey(raw: String?): SyllabusActiveHighlightStyle {
            return values().firstOrNull { it.key == raw } ?: Soft
        }
    }
}

enum class SyllabusWidgetElement(
    val key: String,
    val defaultPosition: Int,
    val defaultScale: WidgetElementScale,
    val defaultVisible: Boolean,
    val defaultSize: Int,
) {
    Day("day", 0, WidgetElementScale.Medium, true, 18),
    Status("status", 1, WidgetElementScale.Medium, true, 16),
    Flow("flow", 2, WidgetElementScale.Large, true, 16),
    Footer("footer", 3, WidgetElementScale.Small, true, 13);

    companion object {
        fun fromKey(raw: String): SyllabusWidgetElement? = values().firstOrNull { it.key == raw }
    }
}
