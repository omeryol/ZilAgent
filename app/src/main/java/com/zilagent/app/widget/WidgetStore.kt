package com.zilagent.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.zilagent.app.util.SpecialDaysCatalog
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object WidgetStore {
    const val PREFS_NAME = "zil_agent_widget_prefs"

    private const val COUNTDOWN_WIDGET_PRESET = "COUNTDOWN_WIDGET_PRESET"
    private const val SYLLABUS_WIDGET_PRESET = "SYLLABUS_WIDGET_PRESET"
    private const val COUNTDOWN_WIDGET_FAMILY = "COUNTDOWN_WIDGET_FAMILY"
    private const val SYLLABUS_WIDGET_FAMILY = "SYLLABUS_WIDGET_FAMILY"
    private const val COUNTDOWN_WIDGET_DENSITY = "COUNTDOWN_WIDGET_DENSITY"
    private const val SYLLABUS_WIDGET_DENSITY = "SYLLABUS_WIDGET_DENSITY"
    private const val COUNTDOWN_WIDGET_TYPOGRAPHY = "COUNTDOWN_WIDGET_TYPOGRAPHY"
    private const val SYLLABUS_WIDGET_TYPOGRAPHY = "SYLLABUS_WIDGET_TYPOGRAPHY"
    private const val GLOBAL_WIDGET_TEXT_SCALE = "GLOBAL_WIDGET_TEXT_SCALE"
    private const val COUNTDOWN_MICRO_ICONS = "COUNTDOWN_MICRO_ICONS"
    private const val SPECIAL_DAYS_LEAD_DAYS = "SPECIAL_DAYS_LEAD_DAYS"
    private const val SPECIAL_DAYS_SELECTED_TEMPLATE_IDS = "SPECIAL_DAYS_SELECTED_TEMPLATE_IDS"
    private const val SPECIAL_DAYS_CUSTOM_ENTRIES = "SPECIAL_DAYS_CUSTOM_ENTRIES"

    data class SpecialReminderEntry(
        val id: String,
        val name: String,
        val startDate: String,
        val endDate: String,
    )

    data class ActiveSpecialReminder(
        val name: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val daysUntilStart: Long,
        val daysUntilEnd: Long,
        val isOngoing: Boolean,
    )
    
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
        triggerAll(context)
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

    fun setWidgetStylePreset(context: Context, preset: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("WIDGET_STYLE_PRESET", preset.coerceIn(0, 3)).apply()
        triggerAll(context)
    }

    fun getWidgetStylePreset(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("WIDGET_STYLE_PRESET", 0).coerceIn(0, 3)
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

    fun setCountdownQuoteGreetingEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("COUNTDOWN_QUOTE_GREETING_ENABLED", enabled).apply()
        triggerAll(context)
    }

    fun isCountdownQuoteGreetingEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("COUNTDOWN_QUOTE_GREETING_ENABLED", true)
    }

    fun setCountdownQuoteSourceEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("COUNTDOWN_QUOTE_SOURCE_ENABLED", enabled).apply()
        triggerAll(context)
    }

    fun isCountdownQuoteSourceEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("COUNTDOWN_QUOTE_SOURCE_ENABLED", true)
    }

    fun setCountdownQuoteAlignment(context: Context, alignment: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("COUNTDOWN_QUOTE_ALIGNMENT", alignment.coerceIn(0, 2)).apply()
        triggerAll(context)
    }

    fun getCountdownQuoteAlignment(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("COUNTDOWN_QUOTE_ALIGNMENT", 0).coerceIn(0, 2)
    }

    fun setCountdownQuoteSourceAlignment(context: Context, alignment: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("COUNTDOWN_QUOTE_SOURCE_ALIGNMENT", alignment.coerceIn(0, 2)).apply()
        triggerAll(context)
    }

    fun getCountdownQuoteSourceAlignment(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("COUNTDOWN_QUOTE_SOURCE_ALIGNMENT", getCountdownQuoteAlignment(context)).coerceIn(0, 2)
    }

    fun setCountdownQuoteTextTone(context: Context, tone: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("COUNTDOWN_QUOTE_TEXT_TONE", tone.coerceIn(0, 3)).apply()
        triggerAll(context)
    }

    fun getCountdownQuoteTextTone(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("COUNTDOWN_QUOTE_TEXT_TONE", 0).coerceIn(0, 3)
    }

    fun setCountdownQuoteSourceTone(context: Context, tone: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("COUNTDOWN_QUOTE_SOURCE_TONE", tone.coerceIn(0, 3)).apply()
        triggerAll(context)
    }

    fun getCountdownQuoteSourceTone(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("COUNTDOWN_QUOTE_SOURCE_TONE", 2).coerceIn(0, 3)
    }

    fun setCountdownQuoteTextBold(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("COUNTDOWN_QUOTE_TEXT_BOLD", enabled).apply()
        triggerAll(context)
    }

    fun isCountdownQuoteTextBold(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("COUNTDOWN_QUOTE_TEXT_BOLD", true)
    }

    fun setCountdownQuoteTextItalic(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("COUNTDOWN_QUOTE_TEXT_ITALIC", enabled).apply()
        triggerAll(context)
    }

    fun isCountdownQuoteTextItalic(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("COUNTDOWN_QUOTE_TEXT_ITALIC", false)
    }

    fun setCountdownQuoteSourceBold(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("COUNTDOWN_QUOTE_SOURCE_BOLD", enabled).apply()
        triggerAll(context)
    }

    fun isCountdownQuoteSourceBold(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("COUNTDOWN_QUOTE_SOURCE_BOLD", false)
    }

    fun setCountdownQuoteSourceItalic(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("COUNTDOWN_QUOTE_SOURCE_ITALIC", enabled).apply()
        triggerAll(context)
    }

    fun isCountdownQuoteSourceItalic(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("COUNTDOWN_QUOTE_SOURCE_ITALIC", false)
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

    fun setSyllabusPaletteScaleEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("SYLLABUS_PALETTE_SCALE_ENABLED", enabled).apply()
        triggerAll(context)
    }

    fun isSyllabusPaletteScaleEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("SYLLABUS_PALETTE_SCALE_ENABLED", true)
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
        triggerAll(context)
    }

    fun getAppLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString("APP_LANGUAGE", "tr") ?: "tr"
        return if (lang in listOf("tr", "en")) lang else "tr"
    }

    fun setWeeklyHideEmptyDays(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("WEEKLY_HIDE_EMPTY_DAYS", enabled).apply()
    }

    fun isWeeklyHideEmptyDays(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("WEEKLY_HIDE_EMPTY_DAYS", false)
    }

    fun setCountdownWidgetPreset(context: Context, preset: WidgetVisualPreset) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(COUNTDOWN_WIDGET_PRESET, preset.key)
            .apply()
        triggerAll(context)
    }

    fun getCountdownWidgetPreset(context: Context): WidgetVisualPreset {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(COUNTDOWN_WIDGET_PRESET, WidgetVisualPreset.Slate.key)
        return WidgetVisualPreset.fromKey(raw)
    }

    fun setSyllabusWidgetPreset(context: Context, preset: WidgetVisualPreset) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SYLLABUS_WIDGET_PRESET, preset.key)
            .apply()
        triggerAll(context)
    }

    fun getSyllabusWidgetPreset(context: Context): WidgetVisualPreset {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SYLLABUS_WIDGET_PRESET, WidgetVisualPreset.Paper.key)
        return WidgetVisualPreset.fromKey(raw)
    }

    fun setCountdownWidgetFamily(context: Context, family: WidgetStyleFamily) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(COUNTDOWN_WIDGET_FAMILY, family.key)
            .apply()
        triggerAll(context)
    }

    fun getCountdownWidgetFamily(context: Context): WidgetStyleFamily {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(COUNTDOWN_WIDGET_FAMILY, WidgetStyleFamily.Minimal.key)
        return WidgetStyleFamily.fromKey(raw)
    }

    fun setSyllabusWidgetFamily(context: Context, family: WidgetStyleFamily) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SYLLABUS_WIDGET_FAMILY, family.key)
            .apply()
        triggerAll(context)
    }

    fun getSyllabusWidgetFamily(context: Context): WidgetStyleFamily {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SYLLABUS_WIDGET_FAMILY, WidgetStyleFamily.Agenda.key)
        return WidgetStyleFamily.fromKey(raw)
    }

    fun setCountdownWidgetDensity(context: Context, density: WidgetInfoDensity) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(COUNTDOWN_WIDGET_DENSITY, density.key)
            .apply()
        triggerAll(context)
    }

    fun getCountdownWidgetDensity(context: Context): WidgetInfoDensity {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(COUNTDOWN_WIDGET_DENSITY, WidgetInfoDensity.Balanced.key)
        return WidgetInfoDensity.fromKey(raw)
    }

    fun setSyllabusWidgetDensity(context: Context, density: WidgetInfoDensity) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SYLLABUS_WIDGET_DENSITY, density.key)
            .apply()
        triggerAll(context)
    }

    fun getSyllabusWidgetDensity(context: Context): WidgetInfoDensity {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SYLLABUS_WIDGET_DENSITY, WidgetInfoDensity.Balanced.key)
        return WidgetInfoDensity.fromKey(raw)
    }

    fun setCountdownTypographyPreset(context: Context, preset: WidgetTypographyPreset) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(COUNTDOWN_WIDGET_TYPOGRAPHY, preset.key)
            .apply()
        triggerAll(context)
    }

    fun getCountdownTypographyPreset(context: Context): WidgetTypographyPreset {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(COUNTDOWN_WIDGET_TYPOGRAPHY, WidgetTypographyPreset.Strong.key)
        return WidgetTypographyPreset.fromKey(raw)
    }

    fun setSyllabusTypographyPreset(context: Context, preset: WidgetTypographyPreset) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SYLLABUS_WIDGET_TYPOGRAPHY, preset.key)
            .apply()
        triggerAll(context)
    }

    fun getSyllabusTypographyPreset(context: Context): WidgetTypographyPreset {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SYLLABUS_WIDGET_TYPOGRAPHY, WidgetTypographyPreset.Notebook.key)
        return WidgetTypographyPreset.fromKey(raw)
    }

    fun setGlobalWidgetTextScale(context: Context, scale: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(GLOBAL_WIDGET_TEXT_SCALE, scale.coerceIn(80, 130))
            .apply()
        triggerAll(context)
    }

    fun getGlobalWidgetTextScale(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(GLOBAL_WIDGET_TEXT_SCALE, 100)
            .coerceIn(80, 130)
    }

    fun setCountdownMicroIconsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(COUNTDOWN_MICRO_ICONS, enabled)
            .apply()
        triggerAll(context)
    }

    fun isCountdownMicroIconsEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(COUNTDOWN_MICRO_ICONS, true)
    }

    fun getCountdownElementPreferences(
        context: Context,
        element: CountdownWidgetElement,
    ): WidgetElementPreferences {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("COUNTDOWN_ELEMENT_${element.key}", null)
        return decodeElementPreferences(
            raw = raw,
            defaultPosition = element.defaultPosition,
            defaultScale = element.defaultScale,
            defaultVisible = element.defaultVisible,
        )
    }

    fun setCountdownElementPreferences(
        context: Context,
        element: CountdownWidgetElement,
        preferences: WidgetElementPreferences,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("COUNTDOWN_ELEMENT_${element.key}", encodeElementPreferences(preferences))
            .apply()
        triggerAll(context)
    }

    fun getCountdownElementSize(
        context: Context,
        element: CountdownWidgetElement,
    ): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("COUNTDOWN_ELEMENT_SIZE_${element.key}", element.defaultSize)
            .coerceIn(0, 50)
    }

    fun setCountdownElementSize(
        context: Context,
        element: CountdownWidgetElement,
        size: Int,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt("COUNTDOWN_ELEMENT_SIZE_${element.key}", size.coerceIn(0, 50))
            .apply()
        triggerAll(context)
    }

    fun resetCountdownElementSizes(context: Context) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        CountdownWidgetElement.values().forEach { element ->
            editor.remove("COUNTDOWN_ELEMENT_SIZE_${element.key}")
        }
        editor.apply()
        triggerAll(context)
    }

    fun getSyllabusElementPreferences(
        context: Context,
        element: SyllabusWidgetElement,
    ): WidgetElementPreferences {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("SYLLABUS_ELEMENT_${element.key}", null)
        return decodeElementPreferences(
            raw = raw,
            defaultPosition = element.defaultPosition,
            defaultScale = element.defaultScale,
            defaultVisible = element.defaultVisible,
        )
    }

    fun setSyllabusElementPreferences(
        context: Context,
        element: SyllabusWidgetElement,
        preferences: WidgetElementPreferences,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("SYLLABUS_ELEMENT_${element.key}", encodeElementPreferences(preferences))
            .apply()
        triggerAll(context)
    }

    fun getSyllabusElementSize(
        context: Context,
        element: SyllabusWidgetElement,
    ): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "SYLLABUS_ELEMENT_SIZE_${element.key}"
        val legacyDefault = when (element) {
            SyllabusWidgetElement.Flow -> getSyllabusFlowTextSize(context)
            SyllabusWidgetElement.Status -> getSyllabusStatusTextSize(context)
            else -> element.defaultSize
        }
        return if (prefs.contains(key)) {
            prefs.getInt(key, legacyDefault)
        } else {
            legacyDefault
        }.coerceIn(0, 50)
    }

    fun setSyllabusElementSize(
        context: Context,
        element: SyllabusWidgetElement,
        size: Int,
    ) {
        val safe = size.coerceIn(0, 50)
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putInt("SYLLABUS_ELEMENT_SIZE_${element.key}", safe)
        when (element) {
            SyllabusWidgetElement.Flow -> editor.putInt("SYLLABUS_FLOW_TEXT_SIZE", safe)
            SyllabusWidgetElement.Status -> editor.putInt("SYLLABUS_STATUS_TEXT_SIZE", safe)
            else -> Unit
        }
        editor.apply()
        triggerAll(context)
    }

    fun resetSyllabusElementSizes(context: Context) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        SyllabusWidgetElement.values().forEach { element ->
            editor.remove("SYLLABUS_ELEMENT_SIZE_${element.key}")
        }
        editor.putInt("SYLLABUS_FLOW_TEXT_SIZE", SyllabusWidgetElement.Flow.defaultSize)
        editor.putInt("SYLLABUS_STATUS_TEXT_SIZE", SyllabusWidgetElement.Status.defaultSize)
        editor.apply()
        triggerAll(context)
    }

    fun setSyllabusActiveHighlightStyle(context: Context, style: SyllabusActiveHighlightStyle) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("SYLLABUS_ACTIVE_HIGHLIGHT_STYLE", style.key)
            .apply()
        triggerAll(context)
    }

    fun getSyllabusActiveHighlightStyle(context: Context): SyllabusActiveHighlightStyle {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("SYLLABUS_ACTIVE_HIGHLIGHT_STYLE", SyllabusActiveHighlightStyle.Soft.key)
        return SyllabusActiveHighlightStyle.fromKey(raw)
    }

    private fun encodeElementPreferences(preferences: WidgetElementPreferences): String {
        val visibleFlag = if (preferences.visible) "1" else "0"
        return listOf(visibleFlag, preferences.position.toString(), preferences.scale.key).joinToString("|")
    }

    private fun decodeElementPreferences(
        raw: String?,
        defaultPosition: Int,
        defaultScale: WidgetElementScale,
        defaultVisible: Boolean,
    ): WidgetElementPreferences {
        if (raw.isNullOrBlank()) {
            return WidgetElementPreferences(
                visible = defaultVisible,
                position = defaultPosition,
                scale = defaultScale,
            )
        }
        val parts = raw.split("|")
        val visible = parts.getOrNull(0) != "0"
        val position = parts.getOrNull(1)?.toIntOrNull() ?: defaultPosition
        val scale = WidgetElementScale.fromKey(parts.getOrNull(2))
        return WidgetElementPreferences(
            visible = visible,
            position = position,
            scale = scale,
        )
    }

    fun setSpecialDaysLeadDays(context: Context, days: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(SPECIAL_DAYS_LEAD_DAYS, days.coerceIn(0, 30))
            .apply()
        triggerAll(context)
    }

    fun getSpecialDaysLeadDays(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(SPECIAL_DAYS_LEAD_DAYS, 7)
            .coerceIn(0, 30)
    }

    fun getSelectedSpecialTemplateIds(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(SPECIAL_DAYS_SELECTED_TEMPLATE_IDS, emptySet())
            ?.toSet()
            ?: emptySet()
    }

    fun setSelectedSpecialTemplate(context: Context, templateId: String, selected: Boolean) {
        val current = getSelectedSpecialTemplateIds(context).toMutableSet()
        if (selected) {
            current.add(templateId)
        } else {
            current.remove(templateId)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(SPECIAL_DAYS_SELECTED_TEMPLATE_IDS, current)
            .apply()
        triggerAll(context)
    }

    fun getCustomSpecialReminders(context: Context): List<SpecialReminderEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SPECIAL_DAYS_CUSTOM_ENTRIES, "")
            .orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split("\n")
            .mapNotNull { line ->
                val parts = line.split("||")
                if (parts.size != 4) return@mapNotNull null
                val id = parts[0]
                val name = parts[1]
                val startDate = parts[2]
                val endDate = parts[3]
                if (id.isBlank() || name.isBlank() || startDate.isBlank() || endDate.isBlank()) return@mapNotNull null
                SpecialReminderEntry(id = id, name = name, startDate = startDate, endDate = endDate)
            }
    }

    fun addCustomSpecialReminder(context: Context, name: String, startDate: String, endDate: String) {
        val entry = SpecialReminderEntry(
            id = "custom_${System.currentTimeMillis()}",
            name = name.trim(),
            startDate = startDate,
            endDate = endDate,
        )
        val all = (getCustomSpecialReminders(context) + entry)
            .takeLast(120)
        persistCustomSpecialReminders(context, all)
    }

    fun removeCustomSpecialReminder(context: Context, id: String) {
        val updated = getCustomSpecialReminders(context).filterNot { it.id == id }
        persistCustomSpecialReminders(context, updated)
    }

    fun getActiveSpecialReminder(context: Context): ActiveSpecialReminder? {
        val isEn = getAppLanguage(context) == "en"
        val today = LocalDate.now()
        val leadDays = getSpecialDaysLeadDays(context)
        val candidates = mutableListOf<ActiveSpecialReminder>()

        getSelectedSpecialTemplateIds(context).forEach { id ->
            val template = SpecialDaysCatalog.byId(id) ?: return@forEach
            val ranges = listOf(today.year - 1, today.year, today.year + 1)
                .map { template.rangeForYear(it) }
            val range = ranges
                .filter { (_, end) -> !end.isBefore(today) }
                .minByOrNull { (start, _) -> ChronoUnit.DAYS.between(today, start).coerceAtLeast(0) }
                ?: return@forEach

            val start = range.first
            val end = range.second
            val daysUntilStart = ChronoUnit.DAYS.between(today, start)
            val daysUntilEnd = ChronoUnit.DAYS.between(today, end)
            val isOngoing = !today.isBefore(start) && !today.isAfter(end)
            if (isOngoing || (daysUntilStart in 0..leadDays.toLong())) {
                candidates += ActiveSpecialReminder(
                    name = template.name(isEn),
                    startDate = start,
                    endDate = end,
                    daysUntilStart = daysUntilStart.coerceAtLeast(0),
                    daysUntilEnd = daysUntilEnd.coerceAtLeast(0),
                    isOngoing = isOngoing,
                )
            }
        }

        getCustomSpecialReminders(context).forEach { entry ->
            val start = runCatching { LocalDate.parse(entry.startDate) }.getOrNull() ?: return@forEach
            val end = runCatching { LocalDate.parse(entry.endDate) }.getOrNull() ?: start
            val normalizedEnd = if (end.isBefore(start)) start else end
            if (normalizedEnd.isBefore(today)) return@forEach

            val daysUntilStart = ChronoUnit.DAYS.between(today, start)
            val daysUntilEnd = ChronoUnit.DAYS.between(today, normalizedEnd)
            val isOngoing = !today.isBefore(start) && !today.isAfter(normalizedEnd)
            if (isOngoing || (daysUntilStart in 0..leadDays.toLong())) {
                candidates += ActiveSpecialReminder(
                    name = entry.name,
                    startDate = start,
                    endDate = normalizedEnd,
                    daysUntilStart = daysUntilStart.coerceAtLeast(0),
                    daysUntilEnd = daysUntilEnd.coerceAtLeast(0),
                    isOngoing = isOngoing,
                )
            }
        }

        return candidates.minWithOrNull(
            compareBy<ActiveSpecialReminder> { if (it.isOngoing) 0 else 1 }
                .thenBy { if (it.isOngoing) it.daysUntilEnd else it.daysUntilStart }
        )
    }

    private fun persistCustomSpecialReminders(context: Context, entries: List<SpecialReminderEntry>) {
        val encoded = entries.joinToString("\n") { entry ->
            listOf(entry.id, entry.name, entry.startDate, entry.endDate).joinToString("||")
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SPECIAL_DAYS_CUSTOM_ENTRIES, encoded)
            .apply()
        triggerAll(context)
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
