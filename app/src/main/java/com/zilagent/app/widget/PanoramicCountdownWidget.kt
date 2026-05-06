package com.zilagent.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.RemoteViews
import com.zilagent.app.MainActivity
import com.zilagent.app.R
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.manager.BellManager
import com.zilagent.app.util.QuoteConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

class PanoramicCountdownWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                BellManager(context).scheduleMinuteTick()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "PanoramicWidget"
        // Switch from live Chronometer to static text with this many seconds left.
        // A larger buffer means guard alarms have more time to fire before the Chronometer
        // can reach zero and start showing negative values.
        private const val COUNTDOWN_TRANSITION_BUFFER_SECONDS = 120L
        // Keep long countdowns in compact static mode to avoid width overflow on widget rows.
        private const val COUNTDOWN_STATIC_LONG_THRESHOLD_SECONDS = 3600L
        private const val COUNTDOWN_GUARD_REQUEST_CODE_PRESTOP = 9972
        private const val COUNTDOWN_GUARD_REQUEST_CODE_PRIMARY = 9973
        private const val COUNTDOWN_GUARD_REQUEST_CODE_SECONDARY = 9974
        // Extra recovery alarms that fire after the target so the widget self-heals quickly
        // even if the primary/secondary guard alarms are delayed by battery optimisation.
        private const val COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_5 = 9975
        private const val COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_30 = 9976
        private const val COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_120 = 9977

        private val badgeIds = intArrayOf(
            R.id.widget_badge_top_start,
            R.id.widget_badge_top_end,
            R.id.widget_badge_center_start,
            R.id.widget_badge_center_end,
            R.id.widget_badge_bottom_start,
            R.id.widget_badge_bottom_end,
        )

        private val metaIds = intArrayOf(
            R.id.widget_meta_top_start,
            R.id.widget_meta_top_end,
            R.id.widget_meta_center_start,
            R.id.widget_meta_center_end,
            R.id.widget_meta_bottom_start,
            R.id.widget_meta_bottom_end,
        )

        private val countdownIds = intArrayOf(
            R.id.widget_countdown_top_start,
            R.id.widget_countdown_top_end,
            R.id.widget_countdown_center_start,
            R.id.widget_countdown_center_end,
            R.id.widget_countdown_bottom_start,
            R.id.widget_countdown_bottom_end,
        )

        private val titleIds = intArrayOf(
            R.id.widget_title_top_start,
            R.id.widget_title_top_end,
            R.id.widget_title_center_start,
            R.id.widget_title_center_end,
            R.id.widget_title_bottom_start,
            R.id.widget_title_bottom_end,
        )

        private val currentIds = intArrayOf(
            R.id.widget_current_top_start,
            R.id.widget_current_top_end,
            R.id.widget_current_center_start,
            R.id.widget_current_center_end,
            R.id.widget_current_bottom_start,
            R.id.widget_current_bottom_end,
        )

        private val nextIds = intArrayOf(
            R.id.widget_next_top_start,
            R.id.widget_next_top_end,
            R.id.widget_next_center_start,
            R.id.widget_next_center_end,
            R.id.widget_next_bottom_start,
            R.id.widget_next_bottom_end,
        )

        private val quoteTitleRows = intArrayOf(
            R.id.widget_quote_title_top_full,
            R.id.widget_quote_title_center_full,
            R.id.widget_quote_title_bottom_full,
        )

        private val quoteSourceRows = intArrayOf(
            R.id.widget_quote_source_top_full,
            R.id.widget_quote_source_center_full,
            R.id.widget_quote_source_bottom_full,
        )

        private data class TodayFrame(
            val hasProfile: Boolean,
            val profileId: Long,
            val dayValue: Int,
            val scheduleCount: Int,
            val previous: BellSchedule?,
            val active: BellSchedule?,
            val next: BellSchedule?,
        )

        private data class CountdownDisplay(
            val badge: String,
            val title: String,
            val meta: String,
            val currentLine: String,
            val nextLine: String,
            val targetMinutes: Int,
            val quoteMode: Boolean = false,
            val holidayMode: Boolean = false,
        )

        suspend fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val isEn = WidgetStore.getAppLanguage(context) == "en"
            val preset = WidgetStore.getCountdownWidgetPreset(context)
            val palette = WidgetAppearance.palette(preset)
            val typography = WidgetStore.getCountdownTypographyPreset(context)

            try {
                val views = RemoteViews(context.packageName, R.layout.widget_dynamic_h)
                WidgetAppearance.applyBackground(context, views, preset)
                hideAllElements(views)

                val display = buildDisplay(context, isEn)
                val secondsRemaining = secondsUntil(display.targetMinutes)
                val countdownColor = WidgetAppearance.countdownAccent(
                    palette = palette,
                    dynamicAccent = WidgetStore.isDynamicColorEnabled(context),
                    secondsRemaining = secondsRemaining,
                )

                val badgePrefs = safePrefs(context, CountdownWidgetElement.Badge)
                val metaPrefs = safePrefs(context, CountdownWidgetElement.Meta)
                val countdownPrefs = safePrefs(context, CountdownWidgetElement.Countdown)
                val titlePrefs = safePrefs(context, CountdownWidgetElement.Title)
                val currentPrefs = safePrefs(context, CountdownWidgetElement.Current)
                val nextPrefs = safePrefs(context, CountdownWidgetElement.Next)

                renderTextElement(
                    views = views,
                    ids = badgeIds,
                    prefs = badgePrefs,
                    text = applyTypography(display.badge, typography, emphasize = true),
                    color = if (display.holidayMode) palette.warning else palette.chipText,
                )

                renderTextElement(
                    views = views,
                    ids = metaIds,
                    prefs = metaPrefs,
                    text = applyTypography(display.meta, typography, emphasize = display.holidayMode),
                    color = if (display.holidayMode) palette.accent else palette.mutedText,
                )

                renderCountdownElement(
                    views = views,
                    ids = countdownIds,
                    prefs = countdownPrefs,
                    secondsRemaining = secondsRemaining,
                    showSeconds = WidgetStore.isShowSeconds(context),
                    isEn = isEn,
                    color = countdownColor,
                )

                if (display.quoteMode) {
                    renderQuoteElement(
                        views = views,
                        ids = quoteTitleRows,
                        rowPosition = rowForPosition(titlePrefs.position),
                        visible = titlePrefs.visible,
                        text = styledText(
                            text = display.title,
                            bold = WidgetStore.isCountdownQuoteTextBold(context),
                            italic = WidgetStore.isCountdownQuoteTextItalic(context),
                        ),
                        color = if (display.holidayMode) palette.warning else palette.text,
                        gravity = quoteGravity(context),
                        extraSizeSp = if (display.holidayMode) 1.5f else 0f,
                    )
                    renderQuoteElement(
                        views = views,
                        ids = quoteSourceRows,
                        rowPosition = rowForPosition(nextPrefs.position),
                        visible = nextPrefs.visible,
                        text = styledText(
                            text = display.nextLine,
                            bold = WidgetStore.isCountdownQuoteSourceBold(context),
                            italic = WidgetStore.isCountdownQuoteSourceItalic(context),
                        ),
                        color = if (display.holidayMode) palette.warning else palette.mutedText,
                        gravity = quoteSourceGravity(context),
                        extraSizeSp = if (display.holidayMode) 1.2f else 0f,
                    )
                } else {
                    renderTextElement(
                        views = views,
                        ids = titleIds,
                        prefs = titlePrefs,
                        text = applyTypography(display.title, typography, emphasize = true),
                        color = palette.text,
                    )
                    renderTextElement(
                        views = views,
                        ids = currentIds,
                        prefs = currentPrefs,
                        text = applyTypography(display.currentLine, typography, emphasize = false),
                        color = palette.footer,
                    )
                    renderTextElement(
                        views = views,
                        ids = nextIds,
                        prefs = nextPrefs,
                        text = applyTypography(display.nextLine, typography, emphasize = false),
                        color = palette.mutedText,
                    )
                }

                applyCountdownElementSizes(context, views)
                if (display.holidayMode) {
                    applyHolidayReadabilityBoost(views)
                }
                syncCountdownGuard(context, display.targetMinutes)
                applyOpenAppIntent(context, views)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (error: Exception) {
                Log.e(TAG, "Widget render failed for id=$appWidgetId", error)
                val fallbackViews = RemoteViews(context.packageName, R.layout.widget_dynamic_h)
                WidgetAppearance.applyBackground(context, fallbackViews, preset)
                hideAllElements(fallbackViews)
                fallbackViews.setViewVisibility(R.id.widget_title_center_start, View.VISIBLE)
                fallbackViews.setViewVisibility(R.id.widget_current_bottom_start, View.VISIBLE)
                fallbackViews.setTextViewText(
                    R.id.widget_title_center_start,
                    if (isEn) "Open the app once" else "Uygulamayı bir kez aç",
                )
                fallbackViews.setTextViewText(
                    R.id.widget_current_bottom_start,
                    if (isEn) "Widget will refresh automatically" else "Widget otomatik yenilenecek",
                )
                fallbackViews.setTextColor(R.id.widget_title_center_start, palette.text)
                fallbackViews.setTextColor(R.id.widget_current_bottom_start, palette.mutedText)
                applyOpenAppIntent(context, fallbackViews)
                appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
            }
        }

        private suspend fun buildDisplay(context: Context, isEn: Boolean): CountdownDisplay {
            val (customEnabled, customTitle, customTime) = WidgetStore.getCustomCountdown(context)
            val quoteSourceEnabled = WidgetStore.isCountdownQuoteSourceEnabled(context)
            val bellManager = BellManager(context)
            val isHoliday = runCatching { bellManager.isHolidayToday() }.getOrDefault(false)
            val todayFrame = if (isHoliday) {
                TodayFrame(
                    hasProfile = true,
                    profileId = -1L,
                    dayValue = LocalDate.now().dayOfWeek.value,
                    scheduleCount = 0,
                    previous = null,
                    active = null,
                    next = null,
                )
            } else {
                loadTodayFrame(context)
            }

            if (customEnabled && customTime != -1 && secondsUntil(customTime) > 0L) {
                return CountdownDisplay(
                    badge = if (isEn) "CUSTOM" else "ÖZEL",
                    title = customTitle.ifBlank { if (isEn) "Custom timer" else "Özel sayaç" },
                    meta = if (isEn) "One-time countdown" else "Tek seferlik sayaç",
                    currentLine = if (isEn) "Now • Focus mode" else "Şu an • Odak modu",
                    nextLine = if (isEn) "Next • ${formatTime(customTime)}" else "Sonra • ${formatTime(customTime)}",
                    targetMinutes = customTime,
                )
            }

            val specialReminder = WidgetStore.getActiveSpecialReminder(context)
            if (specialReminder != null) {
                val periodText = "${formatDate(specialReminder.startDate)} - ${formatDate(specialReminder.endDate)}"
                val meta = if (specialReminder.isOngoing) {
                    if (isEn) {
                        if (specialReminder.daysUntilEnd <= 0L) "Today" else "Ongoing • ${specialReminder.daysUntilEnd}d"
                    } else {
                        if (specialReminder.daysUntilEnd <= 0L) "Bugün" else "Devam • ${specialReminder.daysUntilEnd}g"
                    }
                } else {
                    if (isEn) {
                        if (specialReminder.daysUntilStart <= 0L) "Today" else "Approaching • ${specialReminder.daysUntilStart}d"
                    } else {
                        if (specialReminder.daysUntilStart <= 0L) "Bugün" else "Yaklaşıyor • ${specialReminder.daysUntilStart}g"
                    }
                }
                return CountdownDisplay(
                    badge = if (isEn) "SPECIAL" else "ÖZEL",
                    title = specialReminder.name,
                    meta = meta,
                    currentLine = if (isEn) "Now • Special reminder" else "Şu an • Özel hatırlatma",
                    nextLine = if (isEn) "Date • $periodText" else "Tarih • $periodText",
                    targetMinutes = -1,
                )
            }

            if (isHoliday) {
                val quote = pickWidgetQuote(context, isEn, salt = 3L)
                val holidayName = runCatching { bellManager.getTodayHolidayName() }.getOrNull().orEmpty()
                val remainingDays = todayHolidayRemainingDays(context)
                val holidayLabel = compactHolidayLabel(
                    raw = if (holidayName.isNotBlank()) holidayName else if (isEn) "Holiday" else "Tatil",
                    maxLength = if (isEn) 48 else 48,
                )
                val remainingText = holidayRemainingTextCompact(remainingDays, isEn)
                return CountdownDisplay(
                    badge = if (isEn) "HOLIDAY" else "TATIL",
                    title = quote.content,
                    meta = compactHolidayMetaLine(holidayLabel, remainingText),
                    currentLine = "",
                    nextLine = quoteSource(quoteSourceEnabled, quote.source),
                    targetMinutes = -1,
                    quoteMode = true,
                    holidayMode = true,
                )
            }

            if (!todayFrame.hasProfile) {
                return CountdownDisplay(
                    badge = if (isEn) "PROFILE" else "PROFIL",
                    title = if (isEn) "Choose active profile" else "Aktif profil seç",
                    meta = if (isEn) "Settings > Profiles" else "Ayarlar > Profiller",
                    currentLine = if (isEn) "Now • No active profile" else "Şu an • Aktif profil yok",
                    nextLine = if (isEn) "Next • Add or activate profile" else "Sonra • Profil ekle veya aç",
                    targetMinutes = -1,
                )
            }

            val active = todayFrame.active
            val next = todayFrame.next
            val previous = todayFrame.previous

            if (active != null) {
                return CountdownDisplay(
                    badge = if (active.isBreak) (if (isEn) "BREAK" else "TENEFFÜS") else (if (isEn) "NOW" else "ŞİMDİ"),
                    title = eventDisplayLabel(context, todayFrame, active, isEn),
                    meta = lessonProgressLabel(context, isEn).ifBlank {
                        if (isEn) "Counting to end" else "Bitişe sayılıyor"
                    },
                    currentLine = prefix(isEn, "Now", "Şu an") + eventLine(active, isEn),
                    nextLine = next?.let { prefix(isEn, "Next", "Sonra") + eventLine(it, isEn) }
                        ?: if (isEn) "Next • Day complete" else "Sonra • Gün tamam",
                    targetMinutes = active.endTime,
                )
            }

            if (previous != null && next != null && isLessonGap(previous, next)) {
                return CountdownDisplay(
                    badge = if (isEn) "FREE PERIOD" else "BOŞ DERS",
                    title = if (isEn) "Free Period" else "Boş Ders",
                    meta = if (isEn) "Counting to next lesson" else "Sıradaki derse sayılıyor",
                    currentLine = if (isEn) {
                        "Now • Free ${formatTime(previous.endTime)}-${formatTime(next.startTime)}"
                    } else {
                        "Şu an • Boş ${formatTime(previous.endTime)}-${formatTime(next.startTime)}"
                    },
                    nextLine = prefix(isEn, "Next", "Sonra") + eventLine(next, isEn),
                    targetMinutes = next.startTime,
                )
            }

            if (next != null) {
                return CountdownDisplay(
                    badge = if (isEn) "UP NEXT" else "SIRADAKİ",
                    title = eventDisplayLabel(context, todayFrame, next, isEn),
                    meta = if (isEn) "Counting to start" else "Başlangıca sayılıyor",
                    currentLine = if (isEn) "Now • Waiting" else "Şu an • Bekliyor",
                    nextLine = prefix(isEn, "Next", "Sonra") + eventLine(next, isEn),
                    targetMinutes = next.startTime,
                )
            }

            val quote = pickWidgetQuote(context, isEn, salt = if (todayFrame.scheduleCount > 0) 7L else 11L)
            return CountdownDisplay(
                badge = if (todayFrame.scheduleCount > 0) {
                    if (isEn) "DAY END" else "GÜN SONU"
                } else {
                    if (isEn) "GOOD DAY" else "İYİ GÜNLER"
                },
                title = quote.content,
                meta = if (todayFrame.scheduleCount > 0) {
                    if (isEn) "Today's flow is complete" else "Bugünün akışı tamamlandı"
                } else {
                    if (isEn) "No lessons for today" else "Bugün ders yok"
                },
                currentLine = "",
                nextLine = quoteSource(quoteSourceEnabled, quote.source),
                targetMinutes = -1,
                quoteMode = true,
            )
        }

        private fun safePrefs(context: Context, element: CountdownWidgetElement): WidgetElementPreferences {
            val prefs = WidgetStore.getCountdownElementPreferences(context, element)
            return prefs.copy(position = prefs.position.coerceIn(0, 5))
        }

        private fun renderTextElement(
            views: RemoteViews,
            ids: IntArray,
            prefs: WidgetElementPreferences,
            text: CharSequence,
            color: Int,
        ) {
            hideAll(views, ids)
            if (!prefs.visible || text.isBlank()) return
            val targetId = ids[prefs.position.coerceIn(0, ids.lastIndex)]
            views.setViewVisibility(targetId, View.VISIBLE)
            views.setTextViewText(targetId, text)
            views.setTextColor(targetId, color)
        }

        private fun renderQuoteElement(
            views: RemoteViews,
            ids: IntArray,
            rowPosition: Int,
            visible: Boolean,
            text: CharSequence,
            color: Int,
            gravity: Int,
            extraSizeSp: Float,
        ) {
            hideAll(views, ids)
            if (!visible || text.isBlank()) return
            val targetId = ids[rowPosition.coerceIn(0, ids.lastIndex)]
            views.setViewVisibility(targetId, View.VISIBLE)
            views.setTextViewText(targetId, text)
            views.setTextColor(targetId, color)
            views.setInt(targetId, "setGravity", gravity)
            if (extraSizeSp > 0f) {
                val currentSp = viewsTextSizeSpForId(targetId)
                views.setTextViewTextSize(targetId, TypedValue.COMPLEX_UNIT_SP, currentSp + extraSizeSp)
            }
        }

        private fun renderCountdownElement(
            views: RemoteViews,
            ids: IntArray,
            prefs: WidgetElementPreferences,
            secondsRemaining: Long,
            showSeconds: Boolean,
            isEn: Boolean,
            color: Int,
        ) {
            hideAll(views, ids)
            if (!prefs.visible || secondsRemaining < 0L) return
            val targetId = ids[prefs.position.coerceIn(0, ids.lastIndex)]
            val safeSeconds = secondsRemaining.coerceAtLeast(0L)
            views.setViewVisibility(targetId, View.VISIBLE)
            views.setTextColor(targetId, color)

            val useLiveChronometer =
                showSeconds &&
                    safeSeconds > COUNTDOWN_TRANSITION_BUFFER_SECONDS &&
                    safeSeconds < COUNTDOWN_STATIC_LONG_THRESHOLD_SECONDS

            if (useLiveChronometer) {
                views.setChronometerCountDown(targetId, true)
                views.setChronometer(
                    targetId,
                    SystemClock.elapsedRealtime() + safeSeconds * 1000L,
                    null,
                    true,
                )
            } else {
                views.setChronometerCountDown(targetId, false)
                views.setChronometer(targetId, SystemClock.elapsedRealtime(), null, false)
                views.setTextViewText(targetId, formatDurationForWidget(safeSeconds, showSeconds, isEn))
            }
        }

        private fun hideAllElements(views: RemoteViews) {
            hideAll(views, badgeIds)
            hideAll(views, metaIds)
            hideAll(views, countdownIds)
            hideAll(views, titleIds)
            hideAll(views, currentIds)
            hideAll(views, nextIds)
            hideAll(views, quoteTitleRows)
            hideAll(views, quoteSourceRows)
        }

        private fun hideAll(views: RemoteViews, ids: IntArray) {
            ids.forEach { views.setViewVisibility(it, View.GONE) }
        }

        private fun applyOpenAppIntent(context: Context, views: RemoteViews) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }

        private fun rowForPosition(position: Int): Int = (position.coerceIn(0, 5)) / 2

        private fun applyCountdownElementSizes(context: Context, views: RemoteViews) {
            applyTextSize(views, badgeIds, countdownElementSizeSp(context, CountdownWidgetElement.Badge))
            applyTextSize(views, metaIds, countdownElementSizeSp(context, CountdownWidgetElement.Meta))
            applyTextSize(views, countdownIds, countdownElementSizeSp(context, CountdownWidgetElement.Countdown))
            applyTextSize(views, titleIds, countdownElementSizeSp(context, CountdownWidgetElement.Title))
            applyTextSize(views, currentIds, countdownElementSizeSp(context, CountdownWidgetElement.Current))
            applyTextSize(views, nextIds, countdownElementSizeSp(context, CountdownWidgetElement.Next))
            applyTextSize(views, quoteTitleRows, countdownElementSizeSp(context, CountdownWidgetElement.Title))
            applyTextSize(views, quoteSourceRows, countdownElementSizeSp(context, CountdownWidgetElement.Next))
        }

        private fun applyTextSize(views: RemoteViews, ids: IntArray, sizeSp: Float) {
            ids.toSet().forEach { id ->
                views.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, sizeSp)
            }
        }

        private fun countdownElementSizeSp(context: Context, element: CountdownWidgetElement): Float {
            val slider = WidgetStore.getCountdownElementSize(context, element)
            val base = when (element) {
                CountdownWidgetElement.Badge -> 9f + (slider * 0.22f)
                CountdownWidgetElement.Meta -> 10f + (slider * 0.26f)
                CountdownWidgetElement.Countdown -> 16f + (slider * 0.92f)
                CountdownWidgetElement.Title -> 11f + (slider * 0.36f)
                CountdownWidgetElement.Current,
                CountdownWidgetElement.Next -> 10f + (slider * 0.28f)
                CountdownWidgetElement.Progress -> 10f + (slider * 0.16f)
            }
            val globalScale = WidgetStore.getGlobalWidgetTextScale(context) / 100f
            return (base * globalScale).coerceIn(8f, 72f)
        }

        private fun applyTypography(text: String, typography: WidgetTypographyPreset, emphasize: Boolean): CharSequence {
            if (text.isBlank()) return text
            return SpannableString(text).apply {
                setSpan(TypefaceSpan(typefaceName(typography)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (emphasize || typography == WidgetTypographyPreset.Strong) {
                    setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        private fun styledText(text: String, bold: Boolean, italic: Boolean): CharSequence {
            if (text.isBlank()) return text
            val style = when {
                bold && italic -> Typeface.BOLD_ITALIC
                bold -> Typeface.BOLD
                italic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            if (style == Typeface.NORMAL) return text
            return SpannableString(text).apply {
                setSpan(StyleSpan(style), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        private fun typefaceName(typography: WidgetTypographyPreset): String {
            return when (typography) {
                WidgetTypographyPreset.Soft -> "sans-serif-light"
                WidgetTypographyPreset.Strong -> "sans-serif-medium"
                WidgetTypographyPreset.Technical -> "monospace"
                WidgetTypographyPreset.Notebook -> "serif"
            }
        }

        private suspend fun loadTodayFrame(context: Context): TodayFrame {
            return try {
                val db = AppDatabase.getDatabase(context)
                val profile = db.bellDao().getActiveProfileSync() ?: db.bellDao().getAllProfilesSync().firstOrNull()
                if (profile == null) {
                    TodayFrame(false, -1L, LocalDate.now().dayOfWeek.value, 0, null, null, null)
                } else {
                    val nowSeconds = currentSeconds()
                    val day = LocalDate.now().dayOfWeek.value
                    val schedules = db.bellDao().getSchedulesForProfileSync(profile.id, day).sortedBy { it.startTime }
                    val enriched = schedules.map { schedule ->
                        if (!isCountableLesson(schedule)) {
                            schedule
                        } else {
                            val lessonNo = schedules.count { isCountableLesson(it) && it.startTime <= schedule.startTime }
                            val full = db.syllabusDao().getFullSyllabusEntrySync(profile.id, day, lessonNo)
                            val className = full?.className?.trim().orEmpty()
                            val subjectName = full?.subjectName?.trim().orEmpty()
                            val enrichedName = when {
                                className.isNotBlank() && subjectName.isNotBlank() -> "$className • $subjectName"
                                subjectName.isNotBlank() -> subjectName
                                className.isNotBlank() -> className
                                else -> schedule.name
                            }
                            schedule.copy(name = enrichedName)
                        }
                    }
                    val active = enriched.firstOrNull { nowSeconds >= it.startTime * 60 && nowSeconds < it.endTime * 60 }
                    val previous = enriched.lastOrNull { it.endTime * 60 <= nowSeconds }
                    val next = if (active != null) {
                        enriched.firstOrNull { it.startTime >= active.endTime }
                    } else {
                        enriched.firstOrNull { it.startTime * 60 > nowSeconds }
                    }
                    TodayFrame(true, profile.id, day, schedules.size, previous, active, next)
                }
            } catch (_: Exception) {
                TodayFrame(false, -1L, LocalDate.now().dayOfWeek.value, 0, null, null, null)
            }
        }

        private suspend fun pickWidgetQuote(context: Context, isEn: Boolean, salt: Long): QuoteConstants.WidgetQuote {
            val languageCode = if (isEn) "en" else "tr"
            val fallback = QuoteConstants.getWidgetQuote(languageCode = languageCode, seed = quoteSeed() + salt)
            return try {
                val customQuotes = AppDatabase.getDatabase(context)
                    .quoteDao()
                    .getAllQuotesSync()
                    .filter { !it.isSystem && it.content.isNotBlank() }
                if (customQuotes.isNotEmpty()) {
                    val selected = customQuotes[indexForSeed(customQuotes.size, quoteSeed() + salt)]
                    val parsed = QuoteConstants.parseStoredQuote(selected.content)
                    QuoteConstants.WidgetQuote(
                        content = parsed.content,
                        source = parsed.source.ifBlank { if (isEn) "Your collection" else "Kisisel koleksiyon" },
                    )
                } else {
                    fallback
                }
            } catch (_: Exception) {
                fallback
            }
        }

        private suspend fun eventDisplayLabel(
            context: Context,
            frame: TodayFrame,
            event: BellSchedule,
            isEn: Boolean,
        ): String {
            if (!frame.hasProfile || frame.profileId == -1L || !isCountableLesson(event)) {
                return normalize(event.name, isEn)
            }
            return try {
                val db = AppDatabase.getDatabase(context)
                val schedules = db.bellDao().getSchedulesForProfileSync(frame.profileId, frame.dayValue).sortedBy { it.startTime }
                val lessonNo = schedules.count { isCountableLesson(it) && it.startTime <= event.startTime }
                val full = db.syllabusDao().getFullSyllabusEntrySync(frame.profileId, frame.dayValue, lessonNo)
                val className = full?.className?.trim().orEmpty()
                val subjectName = full?.subjectName?.trim().orEmpty()
                when {
                    className.isNotBlank() && subjectName.isNotBlank() -> "$className • $subjectName"
                    subjectName.isNotBlank() -> subjectName
                    className.isNotBlank() -> className
                    else -> normalize(event.name, isEn)
                }
            } catch (_: Exception) {
                normalize(event.name, isEn)
            }
        }

        private fun eventLine(event: BellSchedule, isEn: Boolean): String {
            return "${normalize(event.name, isEn)} • ${formatTime(event.startTime)}-${formatTime(event.endTime)}"
        }

        private fun lessonProgressLabel(context: Context, isEn: Boolean): String {
            val (currentLesson, totalLessons, isBreak) = WidgetStore.getLessonProgress(context)
            if (currentLesson <= 0 || totalLessons <= 0) return ""
            return if (isEn) {
                if (isBreak) "$currentLesson/$totalLessons break" else "$currentLesson/$totalLessons lesson"
            } else {
                if (isBreak) "$currentLesson/$totalLessons teneffüs" else "$currentLesson/$totalLessons ders"
            }
        }

        private suspend fun todayHolidayRemainingDays(context: Context): Long? {
            val today = LocalDate.now()
            return try {
                val db = AppDatabase.getDatabase(context)
                val holiday = db.holidayDao().getAllHolidaysSync().firstOrNull { item ->
                    val start = LocalDate.parse(item.startDate)
                    val end = LocalDate.parse(item.endDate)
                    !today.isBefore(start) && !today.isAfter(end)
                }
                holiday?.let {
                    val end = LocalDate.parse(it.endDate)
                    ChronoUnit.DAYS.between(today, end).coerceAtLeast(0L)
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun compactHolidayMetaLine(holidayLabel: String, remainingText: String): String {
            return listOf(holidayLabel.trim(), remainingText.trim())
                .filter { it.isNotBlank() }
                .joinToString(" • ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun holidayRemainingTextCompact(remainingDays: Long?, isEn: Boolean): String {
            if (remainingDays == null) return if (isEn) "today" else "bugun"
            return if (isEn) {
                when {
                    remainingDays <= 0L -> "last holiday day"
                    remainingDays == 1L -> "1 day left"
                    else -> "$remainingDays days left"
                }
            } else {
                when {
                    remainingDays <= 0L -> "tatilin son gunu"
                    remainingDays == 1L -> "1 gun kaldi"
                    else -> "$remainingDays gun kaldi"
                }
            }
        }

        private fun compactHolidayLabel(raw: String, maxLength: Int): String {
            val clean = raw.trim().replace(Regex("\\s+"), " ")
            if (clean.length <= maxLength) return clean
            val datePrefix = Regex("^(\\d{1,2}\\s+[A-Za-zCĞIÖŞÜcğıöşü]+)")
                .find(clean)
                ?.groupValues
                ?.getOrNull(1)
                ?.trim()
            if (!datePrefix.isNullOrBlank() && datePrefix.length <= maxLength) {
                return datePrefix
            }
            return clean
        }

        private fun viewsTextSizeSpForId(viewId: Int): Float {
            return when (viewId) {
                R.id.widget_quote_title_top_full,
                R.id.widget_quote_title_center_full,
                R.id.widget_quote_title_bottom_full -> 16f
                R.id.widget_quote_source_top_full,
                R.id.widget_quote_source_center_full,
                R.id.widget_quote_source_bottom_full -> 12f
                else -> 12f
            }
        }

        private fun applyHolidayReadabilityBoost(views: RemoteViews) {
            views.setTextViewTextSize(R.id.widget_quote_title_top_full, TypedValue.COMPLEX_UNIT_SP, 17f)
            views.setTextViewTextSize(R.id.widget_quote_title_center_full, TypedValue.COMPLEX_UNIT_SP, 17f)
            views.setTextViewTextSize(R.id.widget_quote_title_bottom_full, TypedValue.COMPLEX_UNIT_SP, 16f)
            views.setTextViewTextSize(R.id.widget_quote_source_top_full, TypedValue.COMPLEX_UNIT_SP, 13f)
            views.setTextViewTextSize(R.id.widget_quote_source_center_full, TypedValue.COMPLEX_UNIT_SP, 13f)
            views.setTextViewTextSize(R.id.widget_quote_source_bottom_full, TypedValue.COMPLEX_UNIT_SP, 12.5f)
            views.setTextViewTextSize(R.id.widget_meta_top_start, TypedValue.COMPLEX_UNIT_SP, 12f)
            views.setTextViewTextSize(R.id.widget_meta_center_start, TypedValue.COMPLEX_UNIT_SP, 12f)
            views.setTextViewTextSize(R.id.widget_meta_bottom_start, TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        private fun quoteSource(enabled: Boolean, source: String): String {
            return if (enabled) source.trim() else ""
        }

        private fun quoteSeed(): Long {
            val now = LocalTime.now()
            val bucket = (now.hour * 60 + now.minute) / 10
            return LocalDate.now().toEpochDay() * 144L + bucket
        }

        private fun indexForSeed(size: Int, seed: Long): Int {
            val safeSize = size.coerceAtLeast(1)
            val mod = (seed % safeSize).toInt()
            return if (mod >= 0) mod else mod + safeSize
        }

        private fun normalize(raw: String, isEn: Boolean): String {
            val cleaned = raw
                .replace("Giris", "Bitis")
                .replace("Giriş", "Bitis")
                .replace("Teneffus", "Teneffüs")
                .replace("Ogle Arasi", "Öğle Arası")
            return if (isEn) {
                cleaned
                    .replace("Bitis", "Ends")
                    .replace("Ders", "Lesson")
                    .replace("Teneffüs", "Break")
                    .replace("Öğle Arası", "Lunch Break")
            } else {
                cleaned
            }
        }

        private fun isCountableLesson(event: BellSchedule): Boolean {
            val lowered = event.name.lowercase()
            return "ders" in lowered || "lesson" in lowered
        }

        private fun isLessonGap(previous: BellSchedule, next: BellSchedule): Boolean {
            return !previous.isBreak &&
                !next.isBreak &&
                isCountableLesson(previous) &&
                isCountableLesson(next) &&
                previous.endTime < next.startTime
        }

        private fun prefix(isEn: Boolean, en: String, tr: String): String {
            return if (isEn) "$en • " else "$tr • "
        }

        private fun quoteGravity(context: Context): Int {
            return when (WidgetStore.getCountdownQuoteAlignment(context)) {
                1 -> Gravity.CENTER_HORIZONTAL
                2 -> Gravity.END
                else -> Gravity.START
            }
        }

        private fun quoteSourceGravity(context: Context): Int {
            return when (WidgetStore.getCountdownQuoteSourceAlignment(context)) {
                1 -> Gravity.CENTER_HORIZONTAL
                2 -> Gravity.END
                else -> Gravity.START
            }
        }

        private fun syncCountdownGuard(context: Context, targetMinutes: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val prestopIntent = countdownGuardPendingIntent(context, COUNTDOWN_GUARD_REQUEST_CODE_PRESTOP)
            val primaryIntent = countdownGuardPendingIntent(context, COUNTDOWN_GUARD_REQUEST_CODE_PRIMARY)
            val secondaryIntent = countdownGuardPendingIntent(context, COUNTDOWN_GUARD_REQUEST_CODE_SECONDARY)
            val recovery5Intent = countdownGuardPendingIntent(context, COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_5)
            val recovery30Intent = countdownGuardPendingIntent(context, COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_30)
            val recovery120Intent = countdownGuardPendingIntent(context, COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_120)

            if (targetMinutes == -1 || !WidgetStore.isShowSeconds(context)) {
                alarmManager.cancel(prestopIntent)
                alarmManager.cancel(primaryIntent)
                alarmManager.cancel(secondaryIntent)
                alarmManager.cancel(recovery5Intent)
                alarmManager.cancel(recovery30Intent)
                alarmManager.cancel(recovery120Intent)
                return
            }

            // Fires COUNTDOWN_TRANSITION_BUFFER_SECONDS before the target, switching the
            // widget from the live Chronometer to static text before it can reach zero.
            scheduleCountdownGuardAlarm(
                alarmManager = alarmManager,
                requestCode = COUNTDOWN_GUARD_REQUEST_CODE_PRESTOP,
                targetMinutes = targetMinutes,
                offsetSecondsFromTarget = -COUNTDOWN_TRANSITION_BUFFER_SECONDS.toInt(),
                context = context,
            )
            // Fires right at the target to rebuild with the next period.
            scheduleCountdownGuardAlarm(
                alarmManager = alarmManager,
                requestCode = COUNTDOWN_GUARD_REQUEST_CODE_PRIMARY,
                targetMinutes = targetMinutes,
                offsetSecondsFromTarget = 0,
                context = context,
            )
            // One extra attempt 2 s after the target in case PRIMARY was late.
            scheduleCountdownGuardAlarm(
                alarmManager = alarmManager,
                requestCode = COUNTDOWN_GUARD_REQUEST_CODE_SECONDARY,
                targetMinutes = targetMinutes,
                offsetSecondsFromTarget = 2,
                context = context,
            )
            // Recovery alarms ensure the widget self-heals even when the alarms above are
            // delayed by battery optimisation or Doze mode.
            scheduleCountdownGuardAlarm(
                alarmManager = alarmManager,
                requestCode = COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_5,
                targetMinutes = targetMinutes,
                offsetSecondsFromTarget = 5,
                context = context,
            )
            scheduleCountdownGuardAlarm(
                alarmManager = alarmManager,
                requestCode = COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_30,
                targetMinutes = targetMinutes,
                offsetSecondsFromTarget = 30,
                context = context,
            )
            scheduleCountdownGuardAlarm(
                alarmManager = alarmManager,
                requestCode = COUNTDOWN_GUARD_REQUEST_CODE_RECOVERY_120,
                targetMinutes = targetMinutes,
                offsetSecondsFromTarget = 120,
                context = context,
            )
        }

        private fun countdownGuardPendingIntent(context: Context, requestCode: Int): PendingIntent {
            val intent = Intent(context, com.zilagent.app.receiver.BellReceiver::class.java).apply {
                putExtra("IS_WIDGET_UPDATE", true)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun scheduleCountdownGuardAlarm(
            alarmManager: android.app.AlarmManager,
            requestCode: Int,
            targetMinutes: Int,
            offsetSecondsFromTarget: Int,
            context: Context,
        ) {
            val pendingIntent = countdownGuardPendingIntent(context, requestCode)
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetMinutes / 60)
                set(Calendar.MINUTE, targetMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.SECOND, offsetSecondsFromTarget)
            }
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                alarmManager.cancel(pendingIntent)
                return
            }
            try {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } catch (_: SecurityException) {
                try {
                    alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } catch (_: Exception) {
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } catch (_: Exception) {
                alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }

        private fun secondsUntil(targetMinutes: Int): Long {
            if (targetMinutes == -1) return -1L
            return targetMinutes * 60L - LocalTime.now().toSecondOfDay().toLong()
        }

        private fun formatDurationForWidget(totalSeconds: Long, showSeconds: Boolean, isEn: Boolean): String {
            val safeSeconds = totalSeconds.coerceAtLeast(0L)
            val hours = safeSeconds / 3600
            val minutes = (safeSeconds % 3600) / 60
            val seconds = safeSeconds % 60
            return if (showSeconds) {
                // In long countdowns, prefer HH:MM to keep the text compact in narrow widget slots.
                if (hours > 0) "%02d:%02d".format(hours, minutes) else "%02d:%02d".format(minutes, seconds)
            } else {
                if (hours > 0) {
                    "%02d:%02d".format(hours, minutes)
                } else {
                    if (isEn) "%02d min".format(minutes) else "%02d dk".format(minutes)
                }
            }
        }

        private fun formatTime(mins: Int): String {
            val h = mins / 60
            val m = mins % 60
            return "%02d:%02d".format(h, m)
        }

        private fun formatDate(date: LocalDate): String {
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }

        private fun currentSeconds(): Int = LocalTime.now().toSecondOfDay()

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, PanoramicCountdownWidget::class.java))
            CoroutineScope(Dispatchers.IO).launch {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                runCatching { BellManager(context).scheduleMinuteTick() }
            }
        }
    }
}
