package com.zilagent.app.widget

import android.content.Context

object WidgetStore {
    const val PREFS_NAME = "zil_agent_widget_prefs"
    
    fun updateNextBell(context: Context, name: String, targetTime: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("NEXT_BELL_NAME", name)
            putInt("NEXT_BELL_MINUTES", targetTime)
            apply()
        }
        
        // Trigger all types of widgets
        CountdownWidget.updateAllWidgets(context)
        HorizontalCountdownWidget.updateAll(context)
        ModernCountdownWidget.updateAll(context)
        PanoramicCountdownWidget.updateAll(context)
        CircleCountdownWidget.updateAll(context)
    }

    fun getNextBellName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("NEXT_BELL_NAME", null)
    }

    fun getNextBellTime(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("NEXT_BELL_MINUTES", -1)
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

    fun setCustomCountdown(context: Context, enabled: Boolean, title: String, timeMinutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("CUSTOM_MODE_ENABLED", enabled)
            .putString("CUSTOM_MODE_TITLE", title)
            .putInt("CUSTOM_MODE_TIME", timeMinutes)
            .apply()
        
        CountdownWidget.updateAllWidgets(context)
        HorizontalCountdownWidget.updateAll(context)
        ModernCountdownWidget.updateAll(context)
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
        prefs.edit().putString("APP_THEME_COLOR_NAME", colorName).apply()
    }

    fun getThemeColorName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("APP_THEME_COLOR_NAME", "Lavanta") ?: "Lavanta"
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
        
        CountdownWidget.updateAllWidgets(context)
        HorizontalCountdownWidget.updateAll(context)
        ModernCountdownWidget.updateAll(context)
        PanoramicCountdownWidget.updateAll(context)
        CircleCountdownWidget.updateAll(context)
    }

    fun isShowSeconds(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SHOW_SECONDS", true)
    }

    fun setMultilineEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("MULTILINE_ENABLED", enabled).apply()
        setShowSeconds(context, isShowSeconds(context))
    }

    fun isMultilineEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("MULTILINE_ENABLED", false)
    }

    fun setProgressBarEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("PROGRESS_BAR_ENABLED", enabled).apply()
        setShowSeconds(context, isShowSeconds(context))
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

    private fun triggerAll(context: Context) {
        CountdownWidget.updateAllWidgets(context)
        HorizontalCountdownWidget.updateAll(context)
        ModernCountdownWidget.updateAll(context)
        PanoramicCountdownWidget.updateAll(context)
        CircleCountdownWidget.updateAll(context)
    }
}
