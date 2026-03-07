package com.zilagent.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetStore {
    const val PREFS_NAME = "zil_agent_widget_prefs"
    
    fun updateNextBell(context: Context, name: String, targetTime: Int, syllabusInfo: String? = null, classColor: String? = null, startTime: Int = -1) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("NEXT_BELL_NAME", name)
            putInt("NEXT_BELL_MINUTES", targetTime)
            putInt("NEXT_BELL_START_MINUTES", startTime)
            putString("SYLLABUS_INFO", syllabusInfo)
            putString("CLASS_COLOR", classColor)
            apply()
        }
        
        triggerAll(context)
    }

    fun getSyllabusInfo(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("SYLLABUS_INFO", null)
    }

    fun getClassColor(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("CLASS_COLOR", null)
    }

    fun getNextBellName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("NEXT_BELL_NAME", null)
    }

    fun getNextBellTime(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("NEXT_BELL_MINUTES", -1)
    }
    
    fun getNextBellEndTime(context: Context): Int = getNextBellTime(context)
    
    fun getNextBellStartTime(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("NEXT_BELL_START_MINUTES", -1)
    }

    fun setDynamicColorEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("DYNAMIC_COLOR_ENABLED", enabled).apply()
    }

    fun isDynamicColorEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("DYNAMIC_COLOR_ENABLED", false)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("NOTIFICATIONS_ENABLED", enabled).apply()
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("NOTIFICATIONS_ENABLED", false)
    }

    fun setWidgetTextSize(context: Context, size: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_TEXT_SIZE", size).apply()
    }

    fun getWidgetTextSize(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_TEXT_SIZE", 28)
    }

    fun setWidgetLabelTextSize(context: Context, size: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_LABEL_SIZE", size).apply()
    }

    fun getWidgetLabelTextSize(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_LABEL_SIZE", 14)
    }

    fun setWidgetBgOpacity(context: Context, opacity: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_BG_OPACITY", opacity).apply()
    }

    fun getWidgetBgOpacity(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_BG_OPACITY", 90)
    }

    fun setWidgetTextColor(context: Context, colorHex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("WIDGET_TEXT_COLOR", colorHex).apply()
    }

    fun getWidgetTextColor(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("WIDGET_TEXT_COLOR", "#111111") ?: "#111111"
    }

    fun setWidgetBarThickness(context: Context, dp: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_BAR_THICKNESS", dp).apply()
    }

    fun getWidgetBarThickness(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_BAR_THICKNESS", 8)
    }

    fun setWidgetBgColor(context: Context, colorHex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("WIDGET_BG_COLOR", colorHex).apply()
    }

    fun getWidgetBgColor(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("WIDGET_BG_COLOR", "#FFFFFF") ?: "#FFFFFF"
    }

    fun setWidgetCornerRadius(context: Context, radius: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_CORNER_RADIUS", radius).apply()
    }

    fun getWidgetCornerRadius(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_CORNER_RADIUS", 16)
    }

    fun setThemeMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("THEME_MODE", mode).apply()
    }

    fun getThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("THEME_MODE", 0)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SOUND_ENABLED", enabled).apply()
    }

    fun isSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SOUND_ENABLED", true)
    }

    fun setCurrentEventTimes(context: Context, startMinutes: Int, endMinutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("CURRENT_EVENT_START", startMinutes)
            .putInt("CURRENT_EVENT_END", endMinutes)
            .apply()
    }

    fun getCurrentEventTimes(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val start = prefs.getInt("CURRENT_EVENT_START", -1)
        val end = prefs.getInt("CURRENT_EVENT_END", -1)
        return Pair(start, end)
    }

    fun setLessonProgress(context: Context, currentLesson: Int, totalLessons: Int, isBreak: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("LESSON_PROGRESS_CURRENT", currentLesson)
            .putInt("LESSON_PROGRESS_TOTAL", totalLessons)
            .putBoolean("LESSON_PROGRESS_IS_BREAK", isBreak)
            .apply()
    }

    fun getLessonProgress(context: Context): Triple<Int, Int, Boolean> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt("LESSON_PROGRESS_CURRENT", -1)
        val total = prefs.getInt("LESSON_PROGRESS_TOTAL", -1)
        val isBreak = prefs.getBoolean("LESSON_PROGRESS_IS_BREAK", false)
        return Triple(current, total, isBreak)
    }

    fun setCustomCountdown(context: Context, enabled: Boolean, title: String, timeMinutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("CUSTOM_MODE_ENABLED", enabled)
            .putString("CUSTOM_MODE_TITLE", title)
            .putInt("CUSTOM_MODE_TIME", timeMinutes)
            .apply()
        
        triggerAll(context)
    }

    fun getCustomCountdown(context: Context): Triple<Boolean, String, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("CUSTOM_MODE_ENABLED", false)
        val title = prefs.getString("CUSTOM_MODE_TITLE", "") ?: ""
        val time = prefs.getInt("CUSTOM_MODE_TIME", -1)
        return Triple(enabled, title, time)
    }

    fun setThemeColorName(context: Context, colorName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("THEME_COLOR", colorName).apply()
    }

    fun getThemeColorName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("THEME_COLOR", "Lavanta") ?: "Lavanta"
    }

    fun hasCompletedOnboarding(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("HAS_COMPLETED_ONBOARDING", false)
    }

    fun setOnboardingCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("HAS_COMPLETED_ONBOARDING", true).apply()
    }

    fun getWorkingDays(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("WORKING_DAYS_MASK", "1111100") ?: "1111100"
    }

    fun setWorkingDays(context: Context, mask: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("WORKING_DAYS_MASK", mask).apply()
    }

    fun setWidgetLayoutType(context: Context, type: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_LAYOUT_TYPE", type).apply()
    }

    fun getWidgetLayoutType(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_LAYOUT_TYPE", 0)
    }

    fun setAutoSilentMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("AUTO_SILENT_MODE", enabled).apply()
    }

    fun isAutoSilentMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("AUTO_SILENT_MODE", false)
    }

    fun setShowSeconds(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SHOW_SECONDS", enabled).apply()
        triggerAll(context)
    }

    fun isShowSeconds(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SHOW_SECONDS", true)
    }

    fun setMultilineEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("MULTILINE_ENABLED", enabled).apply()
        triggerAll(context)
    }

    fun isMultilineEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("MULTILINE_ENABLED", false)
    }

    fun setProgressBarEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("PROGRESS_BAR_ENABLED", enabled).apply()
        triggerAll(context)
    }

    fun isProgressBarEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("PROGRESS_BAR_ENABLED", true)
    }

    // New Customization Settings
    fun setWidgetFlowDirection(context: Context, direction: Int) { // 0: Vertical, 1: Horizontal
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_FLOW_DIRECTION", direction).apply()
        triggerAll(context)
    }

    fun getWidgetFlowDirection(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_FLOW_DIRECTION", 0)
    }

    fun setWidgetAlignment(context: Context, alignment: Int) { // 0: Left, 1: Center, 2: Right
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_ALIGNMENT", alignment).apply()
        triggerAll(context)
    }

    fun getWidgetAlignment(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_ALIGNMENT", 1)
    }

    fun setWidgetSpacing(context: Context, spacing: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_SPACING", spacing).apply()
        triggerAll(context)
    }

    fun getWidgetSpacing(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_SPACING", 8)
    }

    fun setWidgetElementOrder(context: Context, order: Int) { // 0: Time First, 1: Label First
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_ELEMENT_ORDER", order).apply()
        triggerAll(context)
    }

    fun getWidgetElementOrder(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_ELEMENT_ORDER", 0)
    }

    fun setPanoramicTimeTextSize(context: Context, size: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("PANORAMIC_TIME_TEXT_SIZE", size).apply()
        triggerAll(context)
    }

    fun getPanoramicTimeTextSize(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("PANORAMIC_TIME_TEXT_SIZE", 30)
    }

    fun setPanoramicTitleTextSize(context: Context, size: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("PANORAMIC_TITLE_TEXT_SIZE", size).apply()
        triggerAll(context)
    }

    fun getPanoramicTitleTextSize(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("PANORAMIC_TITLE_TEXT_SIZE", 15)
    }

    fun setSyllabusFlowTextSize(context: Context, size: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("SYLLABUS_FLOW_TEXT_SIZE", size).apply()
        triggerAll(context)
    }

    fun getSyllabusFlowTextSize(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("SYLLABUS_FLOW_TEXT_SIZE", 15)
    }

    fun setSyllabusStatusTextSize(context: Context, size: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("SYLLABUS_STATUS_TEXT_SIZE", size).apply()
        triggerAll(context)
    }

    fun getSyllabusStatusTextSize(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("SYLLABUS_STATUS_TEXT_SIZE", 15)
    }

    fun setSyllabusShowIcons(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SYLLABUS_SHOW_ICONS", enabled).apply()
        triggerAll(context)
    }

    fun isSyllabusShowIcons(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SYLLABUS_SHOW_ICONS", true)
    }

    fun setSyllabusShowClassColors(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SYLLABUS_SHOW_CLASS_COLORS", enabled).apply()
        triggerAll(context)
    }

    fun isSyllabusShowClassColors(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SYLLABUS_SHOW_CLASS_COLORS", true)
    }

    fun setSyllabusShowBreaks(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SYLLABUS_SHOW_BREAKS", enabled).apply()
        triggerAll(context)
    }

    fun isSyllabusShowBreaks(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SYLLABUS_SHOW_BREAKS", true)
    }

    fun setSyllabusShowTimes(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SYLLABUS_SHOW_TIMES", enabled).apply()
        triggerAll(context)
    }

    fun isSyllabusShowTimes(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SYLLABUS_SHOW_TIMES", true)
    }

    fun setSyllabusColorizeText(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SYLLABUS_COLORIZE_TEXT", enabled).apply()
        triggerAll(context)
    }

    fun isSyllabusColorizeText(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SYLLABUS_COLORIZE_TEXT", true)
    }

    fun setDashboardMotionEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("DASHBOARD_MOTION_ENABLED", enabled).apply()
    }

    fun isDashboardMotionEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getBoolean("DASHBOARD_MOTION_ENABLED", true)
        } catch (_: Exception) {
            true
        }
    }

    fun setDashboardMotionStrength(context: Context, strength: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("DASHBOARD_MOTION_STRENGTH", strength.coerceIn(5, 60)).apply()
    }

    fun getDashboardMotionStrength(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getInt("DASHBOARD_MOTION_STRENGTH", 26).coerceIn(5, 60)
        } catch (_: Exception) {
            26
        }
    }

    fun setDashboardCountdownTextSize(context: Context, size: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("DASHBOARD_COUNTDOWN_TEXT_SIZE", size.coerceIn(40, 120)).apply()
    }

    fun getDashboardCountdownTextSize(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getInt("DASHBOARD_COUNTDOWN_TEXT_SIZE", 62).coerceIn(40, 120)
        } catch (_: Exception) {
            62
        }
    }

    fun setDashboardCardBorderWidth(context: Context, widthDp: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("DASHBOARD_CARD_BORDER_WIDTH", widthDp.coerceIn(1, 8)).apply()
    }

    fun getDashboardCardBorderWidth(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getInt("DASHBOARD_CARD_BORDER_WIDTH", 2).coerceIn(1, 8)
        } catch (_: Exception) {
            2
        }
    }

    fun setTouchAnimationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("TOUCH_ANIMATIONS_ENABLED", enabled).apply()
    }

    fun isTouchAnimationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getBoolean("TOUCH_ANIMATIONS_ENABLED", true)
        } catch (_: Exception) {
            true
        }
    }

    fun setTouchAnimationIntensity(context: Context, intensity: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("TOUCH_ANIMATIONS_INTENSITY", intensity.coerceIn(10, 100)).apply()
    }

    fun getTouchAnimationIntensity(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getInt("TOUCH_ANIMATIONS_INTENSITY", 60).coerceIn(10, 100)
        } catch (_: Exception) {
            60
        }
    }

    fun setTouchAnimationStyle(context: Context, style: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("TOUCH_ANIMATIONS_STYLE", style.coerceIn(0, 2)).apply()
    }

    fun getTouchAnimationStyle(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getInt("TOUCH_ANIMATIONS_STYLE", 0).coerceIn(0, 2)
        } catch (_: Exception) {
            0
        }
    }

    fun setAppBackgroundMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("APP_BACKGROUND_MODE", mode.coerceIn(0, 4)).apply()
    }

    fun getAppBackgroundMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getInt("APP_BACKGROUND_MODE", 0).coerceIn(0, 4)
        } catch (_: Exception) {
            0
        }
    }

    fun setAppLanguage(context: Context, languageCode: String) {
        val safe = if (languageCode.lowercase() == "en") "en" else "tr"
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("APP_LANGUAGE", safe).apply()
    }

    fun getAppLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("APP_LANGUAGE", "tr") ?: "tr"
    }

    private fun triggerAll(context: Context) {
        PanoramicCountdownWidget.updateAll(context)
        SyllabusWidget.updateAll(context)
        try {
            val intent = Intent("com.zilagent.app.WIDGET_UPDATE")
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {}
    }
}
