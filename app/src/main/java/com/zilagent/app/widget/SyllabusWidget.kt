package com.zilagent.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.util.TypedValue
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.zilagent.app.MainActivity
import com.zilagent.app.R
import com.zilagent.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.time.LocalDate
import java.util.Calendar

class SyllabusWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                com.zilagent.app.manager.BellManager(context).scheduleMinuteTick()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SyllabusWidget"
        private const val FREE_PERIOD_EN = "Free Period"
        private const val FREE_PERIOD_TR = "Bo\u015F Ders"

        private val dayIds = intArrayOf(
            R.id.widget_day_header,
            R.id.widget_day_focus,
            R.id.widget_day_detail,
            R.id.widget_day_footer,
        )

        private val statusIds = intArrayOf(
            R.id.widget_status_header,
            R.id.widget_status_focus,
            R.id.widget_status_detail,
            R.id.widget_status_footer,
        )

        private val flowIds = intArrayOf(
            R.id.widget_flow_header,
            R.id.widget_flow_focus,
            R.id.widget_flow_detail,
            R.id.widget_flow_footer,
        )

        private val flowBackgroundIds = intArrayOf(
            R.id.widget_flow_bg_header,
            R.id.widget_flow_bg_focus,
            R.id.widget_flow_bg_detail,
            R.id.widget_flow_bg_footer,
        )

        private val footerIds = intArrayOf(
            R.id.widget_footer_header,
            R.id.widget_footer_focus,
            R.id.widget_footer_detail,
            R.id.widget_footer_footer,
        )

        private data class SyllabusDisplay(
            val day: String,
            val dayColor: Int,
            val status: String,
            val flow: CharSequence,
            val footer: String,
            val statusColor: Int,
            val flowColor: Int,
            val footerColor: Int,
        )

        private data class DayFlow(
            val profileId: Long,
            val dayValue: Int,
            val offset: Int,
            val all: List<com.zilagent.app.data.entity.BellSchedule>,
            val visible: List<com.zilagent.app.data.entity.BellSchedule>,
        )

        private data class FlowEntry(
            val startTime: Int,
            val endTime: Int,
            val label: String,
            val isBreak: Boolean,
            val isGap: Boolean = false,
            val isEmptyLesson: Boolean = false,
            val isActive: Boolean = false,
            val lessonNumber: Int? = null,
            val classColor: Int? = null,
            val classLabel: String = "",
            val subjectLabel: String = "",
        )

        private data class FlowRender(
            val text: CharSequence,
        )

        suspend fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val isEn = WidgetStore.getAppLanguage(context) == "en"
            val preset = WidgetStore.getSyllabusWidgetPreset(context)
            val palette = WidgetAppearance.palette(preset)
            val typography = WidgetStore.getSyllabusTypographyPreset(context)

            try {
                val views = RemoteViews(context.packageName, R.layout.widget_syllabus)
                WidgetAppearance.applyBackground(context, views, preset)
                hideAll(views, dayIds)
                hideAll(views, statusIds)
                hideAll(views, flowIds)
                hideAll(views, flowBackgroundIds)
                hideAll(views, footerIds)

                val flowPrefs = WidgetStore.getSyllabusElementPreferences(context, SyllabusWidgetElement.Flow)
                val display = buildDisplay(context, isEn, palette)
                renderTextElement(
                    views = views,
                    ids = dayIds,
                    prefs = WidgetStore.getSyllabusElementPreferences(context, SyllabusWidgetElement.Day),
                    text = styledStaticText(display.day, typography, emphasize = true),
                    color = display.dayColor,
                )
                renderTextElement(
                    views = views,
                    ids = statusIds,
                    prefs = WidgetStore.getSyllabusElementPreferences(context, SyllabusWidgetElement.Status),
                    text = styledStaticText(display.status, typography, emphasize = true),
                    color = display.statusColor,
                )
                renderTextElement(
                    views = views,
                    ids = flowIds,
                    prefs = flowPrefs,
                    text = styledFlowText(display.flow, typography),
                    color = display.flowColor,
                )
                renderTextElement(
                    views = views,
                    ids = footerIds,
                    prefs = WidgetStore.getSyllabusElementPreferences(context, SyllabusWidgetElement.Footer),
                    text = styledStaticText(display.footer, typography, emphasize = false),
                    color = display.footerColor,
                )
                applyTextSize(views, dayIds, syllabusElementSizeSp(context, SyllabusWidgetElement.Day))
                applyTextSize(views, statusIds, syllabusElementSizeSp(context, SyllabusWidgetElement.Status))
                applyTextSize(views, flowIds, syllabusElementSizeSp(context, SyllabusWidgetElement.Flow))
                applyTextSize(views, footerIds, syllabusElementSizeSp(context, SyllabusWidgetElement.Footer))

                applyOpenAppIntent(context, views)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (error: Exception) {
                Log.e(TAG, "Widget render failed for id=$appWidgetId", error)
                val fallbackViews = RemoteViews(context.packageName, R.layout.widget_syllabus)
                WidgetAppearance.applyBackground(context, fallbackViews, preset)
                hideAll(fallbackViews, dayIds)
                hideAll(fallbackViews, statusIds)
                hideAll(fallbackViews, flowIds)
                hideAll(fallbackViews, flowBackgroundIds)
                hideAll(fallbackViews, footerIds)
                fallbackViews.setViewVisibility(R.id.widget_day_focus, View.VISIBLE)
                fallbackViews.setViewVisibility(R.id.widget_status_detail, View.VISIBLE)
                fallbackViews.setTextViewText(
                    R.id.widget_day_focus,
                    if (isEn) "Weekly flow" else "Haftalık akış",
                )
                fallbackViews.setTextColor(R.id.widget_day_focus, palette.text)
                fallbackViews.setTextViewText(
                    R.id.widget_status_detail,
                    if (isEn) "Open the app once to refresh" else "Yenilemek için uygulamayı bir kez aç",
                )
                fallbackViews.setTextColor(R.id.widget_status_detail, palette.mutedText)
                applyOpenAppIntent(context, fallbackViews)
                appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
            }
        }

        private fun applyOpenAppIntent(context: Context, views: RemoteViews) {
            val appIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }

        private suspend fun buildDisplay(
            context: Context,
            isEn: Boolean,
            palette: WidgetPalette,
        ): SyllabusDisplay {
            val showIcons = WidgetStore.isSyllabusShowIcons(context)
            val showClassColors = WidgetStore.isSyllabusShowClassColors(context)
            val showBreaks = WidgetStore.isSyllabusShowBreaks(context)
            val showTimes = WidgetStore.isSyllabusShowTimes(context)
            val colorizeText = WidgetStore.isSyllabusColorizeText(context)
            val paletteScaleEnabled = WidgetStore.isSyllabusPaletteScaleEnabled(context)
            val activeHighlightStyle = WidgetStore.getSyllabusActiveHighlightStyle(context)
            val styleFamily = WidgetStore.getSyllabusWidgetFamily(context)
            val density = WidgetStore.getSyllabusWidgetDensity(context)
            val typography = WidgetStore.getSyllabusTypographyPreset(context)
            val flowElementSize = WidgetStore.getSyllabusElementSize(context, SyllabusWidgetElement.Flow)
            val nowCal = Calendar.getInstance()
            val nowMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
            val todayValue = LocalDate.now().dayOfWeek.value
            val todayName = dayName(todayValue, isEn)
            val isHoliday = try { com.zilagent.app.manager.BellManager(context).isHolidayToday() } catch (_: Exception) { false }

            return try {
                val db = AppDatabase.getDatabase(context)
                    val profile = db.bellDao().getActiveProfileSync() ?: db.bellDao().getAllProfilesSync().firstOrNull()
                    if (profile == null) {
                        return SyllabusDisplay(
                            day = todayName,
                            dayColor = palette.text,
                            status = if (isEn) "No active profile" else "Aktif profil yok",
                            flow = if (isEn) "Settings > Profiles" else "Ayarlar > Profiller",
                            footer = if (isEn) "Add a profile to populate the flow" else "Akışı doldurmak için profil ekle",
                            statusColor = palette.warning,
                            flowColor = palette.footer,
                            footerColor = subtleFooterColor(palette),
                        )
                    }

                    val todayAll = db.bellDao().getSchedulesForProfileSync(profile.id, todayValue).sortedBy { it.startTime }
                    val todayActive = todayAll.firstOrNull { nowMinutes >= it.startTime && nowMinutes < it.endTime }
                    val todayRemaining = todayAll.filter { it.endTime > nowMinutes }
                    val todayVisible = if (showBreaks) todayRemaining else todayRemaining.filter { !it.isBreak }
                    val activeVisible = if (!showBreaks && todayActive?.isBreak == true) null else todayActive
                    val lessonProgress = WidgetStore.getLessonProgress(context)
                    val todayHeader = buildString {
                        append(todayName)
                        if (lessonProgress.first > 0 && lessonProgress.second > 0) {
                            append(" • ")
                            append(lessonProgress.first)
                            append("/")
                            append(lessonProgress.second)
                        }
                    }

                    val styledTodayHeader = buildDayHeader(
                        todayName = todayName,
                        currentLesson = lessonProgress.first,
                        totalLessons = lessonProgress.second,
                        family = styleFamily,
                        isEn = isEn,
                    )

                    if (isHoliday) {
                        val nextFlow = findNextDayFlow(db, profile.id, todayValue, showBreaks)
                        if (nextFlow != null) {
                            return previewDayDisplay(
                                nextFlow = nextFlow,
                                status = if (isEn) "Holiday • Upcoming flow ready" else "Tatil • Sonraki akış hazır",
                                footer = if (isEn) "The day has not changed yet" else "Gün henüz değişmedi",
                                dayColor = palette.warning,
                                statusColor = palette.warning,
                                palette = palette,
                                colorizeText = colorizeText,
                                showIcons = showIcons,
                                showTimes = showTimes,
                                showClassColors = showClassColors,
                                usePaletteScale = paletteScaleEnabled,
                                highlightStyle = activeHighlightStyle,
                                family = styleFamily,
                                density = density,
                                typography = typography,
                                flowElementSize = flowElementSize,
                                db = db,
                                isEn = isEn,
                            )
                        }

                        return SyllabusDisplay(
                            day = styledTodayHeader,
                            dayColor = palette.warning,
                            status = if (isEn) "Holiday today" else "Bugün tatil",
                            flow = if (isEn) "Take a breath and enjoy the pause" else "Bir nefes al ve molanın tadını çıkar",
                            footer = if (isEn) "Flow resumes on the next school day" else "Akış bir sonraki okul gününde döner",
                            statusColor = palette.warning,
                            flowColor = palette.footer,
                            footerColor = subtleFooterColor(palette),
                        )
                    }

                    if (todayAll.isEmpty()) {
                        val nextFlow = findNextDayFlow(db, profile.id, todayValue, showBreaks)
                        if (nextFlow != null) {
                            return previewDayDisplay(
                                nextFlow = nextFlow,
                                status = if (isEn) "No lessons today • Upcoming flow ready" else "Bugün ders yok • Sonraki akış hazır",
                                footer = if (isEn) "The day has not changed yet" else "Gün henüz değişmedi",
                                dayColor = palette.warning,
                                statusColor = palette.warning,
                                palette = palette,
                                colorizeText = colorizeText,
                                showIcons = showIcons,
                                showTimes = showTimes,
                                showClassColors = showClassColors,
                                usePaletteScale = paletteScaleEnabled,
                                highlightStyle = activeHighlightStyle,
                                family = styleFamily,
                                density = density,
                                typography = typography,
                                flowElementSize = flowElementSize,
                                db = db,
                                isEn = isEn,
                            )
                        }

                        return SyllabusDisplay(
                            day = styledTodayHeader,
                            dayColor = palette.text,
                            status = if (isEn) "No lessons today" else "Bugün ders yok",
                            flow = if (isEn) "This day has no assigned lesson flow" else "Bu gün için atanmış ders akışı yok",
                            footer = if (isEn) "Use profiles to add schedules" else "Program eklemek için profilleri kullan",
                            statusColor = palette.warning,
                            flowColor = palette.footer,
                            footerColor = subtleFooterColor(palette),
                        )
                    }

                    if (activeVisible == null && todayVisible.isEmpty()) {
                        val nextFlow = findNextDayFlow(db, profile.id, todayValue, showBreaks)
                        if (nextFlow != null) {
                            return previewDayDisplay(
                                nextFlow = nextFlow,
                                status = if (isEn) "Day end • Showing the next day" else "Gün sonu • Sonraki gün gösteriliyor",
                                footer = if (isEn) "The day has not changed yet" else "Gün henüz değişmedi",
                                dayColor = palette.warning,
                                statusColor = palette.warning,
                                palette = palette,
                                colorizeText = colorizeText,
                                showIcons = showIcons,
                                showTimes = showTimes,
                                showClassColors = showClassColors,
                                usePaletteScale = paletteScaleEnabled,
                                highlightStyle = activeHighlightStyle,
                                family = styleFamily,
                                density = density,
                                typography = typography,
                                flowElementSize = flowElementSize,
                                db = db,
                                isEn = isEn,
                            )
                        }

                        return SyllabusDisplay(
                            day = styledTodayHeader,
                            dayColor = palette.text,
                            status = if (isEn) "Day complete" else "Gün tamam",
                            flow = if (isEn) "No remaining items for today" else "Bugün için kalan olay yok",
                            footer = if (isEn) "Open the app for tomorrow's flow" else "Yarının akışı için uygulamayı aç",
                            statusColor = palette.warning,
                            flowColor = palette.footer,
                            footerColor = subtleFooterColor(palette),
                        )
                    }

                    val status = when {
                        activeVisible != null && activeVisible.isBreak -> {
                            if (isEn) "Now • ${normalize(activeVisible.name, isEn)}" else "Şu an • ${normalize(activeVisible.name, isEn)}"
                        }

                        activeVisible != null -> {
                            val label = if (isCountableLesson(activeVisible)) {
                                val lessonNo = lessonNo(todayAll, activeVisible)
                                val full = db.syllabusDao().getFullSyllabusEntrySync(profile.id, todayValue, lessonNo)
                                buildCompactLessonLabel(lessonNo, activeVisible.name, full, showClassColors, isEn)
                            } else {
                                normalize(activeVisible.name, isEn)
                            }
                            if (isEn) "Now • $label" else "Şu an • $label"
                        }

                        else -> {
                            val next = todayVisible.first()
                            if (next.isBreak) {
                                if (isEn) "Next • ${normalize(next.name, isEn)}" else "Sonra • ${normalize(next.name, isEn)}"
                            } else {
                                val label = if (isCountableLesson(next)) {
                                    val lessonNo = lessonNo(todayAll, next)
                                    val full = db.syllabusDao().getFullSyllabusEntrySync(profile.id, todayValue, lessonNo)
                                    buildCompactLessonLabel(lessonNo, next.name, full, showClassColors, isEn)
                                } else {
                                    normalize(next.name, isEn)
                                }
                                if (isEn) "Next • $label" else "Sonra • $label"
                            }
                        }
                    }
                    val statusDisplay = buildStatusLine(
                        kind = when {
                            activeVisible?.isBreak == true -> "break"
                            activeVisible != null -> "now"
                            else -> "next"
                        },
                        body = extractStatusBody(status),
                        family = styleFamily,
                        showIcons = showIcons,
                        isEn = isEn,
                    )

                    val statusColor = when {
                        activeVisible?.isBreak == true -> palette.warning
                        activeVisible != null -> palette.success
                        else -> palette.accent
                    }

                    val flowItems = if (activeVisible != null) {
                        todayVisible
                    } else {
                        trimPrimaryItem(
                            items = todayVisible,
                            primary = todayVisible.firstOrNull(),
                        )
                    }
                    val flow = buildFlowText(
                        items = flowItems,
                        all = todayAll,
                        profileId = profile.id,
                        day = todayValue,
                        db = db,
                        baseColor = if (colorizeText) statusColor else palette.footer,
                        gapColor = palette.warning,
                        palette = palette,
                        activeId = activeVisible?.id,
                        highlightStyle = activeHighlightStyle,
                        showIcons = showIcons,
                        showTimes = showTimes,
                        showClassColors = showClassColors,
                        usePaletteScale = paletteScaleEnabled,
                        typography = typography,
                        maxEntries = flowLineBudget(flowElementSize, density),
                        maxLabelChars = flowLabelBudget(flowElementSize, showTimes, density, typography),
                        isEn = isEn,
                    )

                    val footer = when {
                        activeVisible != null -> {
                            val left = (todayVisible.size - 1).coerceAtLeast(0)
                            if (isEn) "$left upcoming items left" else "$left kalan olay var"
                        }

                        else -> {
                            val next = todayVisible.first()
                            if (isEn) "First upcoming • ${formatTime(next.startTime)}" else "İlk sıradaki • ${formatTime(next.startTime)}"
                        }
                    }

                    val footerDisplay = buildFooterLine(
                        primary = normalizeSeparators(footer),
                        family = styleFamily,
                        density = density,
                        isEn = isEn,
                    )

                    SyllabusDisplay(
                        day = styledTodayHeader,
                        dayColor = palette.text,
                        status = statusDisplay,
                        flow = flow.text,
                        footer = footerDisplay,
                        statusColor = statusColor,
                        flowColor = if (colorizeText) statusColor else palette.footer,
                        footerColor = subtleFooterColor(palette),
                    )
            } catch (_: Exception) {
                SyllabusDisplay(
                    day = todayName,
                    dayColor = palette.text,
                    status = if (isEn) "Widget data unavailable" else "Widget verisi okunamadı",
                    flow = if (isEn) "Open the app and try again" else "Uygulamayı açıp tekrar dene",
                    footer = if (isEn) "The flow will refresh on the next sync" else "Akış bir sonraki eşitlemede yenilenir",
                    statusColor = palette.warning,
                    flowColor = palette.footer,
                    footerColor = subtleFooterColor(palette),
                )
            }
        }

        private suspend fun previewDayDisplay(
            nextFlow: DayFlow,
            status: String,
            footer: String,
            dayColor: Int,
            statusColor: Int,
            palette: WidgetPalette,
            colorizeText: Boolean,
            showIcons: Boolean,
            showTimes: Boolean,
            showClassColors: Boolean,
            usePaletteScale: Boolean,
            highlightStyle: SyllabusActiveHighlightStyle,
            family: WidgetStyleFamily,
            density: WidgetInfoDensity,
            typography: WidgetTypographyPreset,
            flowElementSize: Int,
            db: AppDatabase,
            isEn: Boolean,
        ): SyllabusDisplay {
            val flow = buildFlowText(
                items = nextFlow.visible,
                all = nextFlow.all,
                profileId = nextFlow.profileId,
                day = nextFlow.dayValue,
                db = db,
                baseColor = if (colorizeText) palette.accent else palette.footer,
                gapColor = palette.warning,
                palette = palette,
                activeId = null,
                highlightStyle = highlightStyle,
                showIcons = showIcons,
                showTimes = showTimes,
                showClassColors = showClassColors,
                usePaletteScale = usePaletteScale,
                typography = typography,
                maxEntries = flowLineBudget(flowElementSize, density),
                maxLabelChars = flowLabelBudget(flowElementSize, showTimes, density, typography),
                isEn = isEn,
            )
            return SyllabusDisplay(
                day = buildPreviewDayLabel(nextFlow.dayValue, nextFlow.offset, family, isEn),
                dayColor = dayColor,
                status = status,
                flow = flow.text,
                footer = buildFooterLine(footer, family, density, isEn),
                statusColor = statusColor,
                flowColor = if (colorizeText) palette.accent else palette.footer,
                footerColor = subtleFooterColor(palette),
            )
        }

        private suspend fun buildFlowText(
            items: List<com.zilagent.app.data.entity.BellSchedule>,
            all: List<com.zilagent.app.data.entity.BellSchedule>,
            profileId: Long,
            day: Int,
            db: AppDatabase,
            baseColor: Int,
            gapColor: Int,
            palette: WidgetPalette,
            activeId: Long?,
            highlightStyle: SyllabusActiveHighlightStyle,
            showIcons: Boolean,
            showTimes: Boolean,
            showClassColors: Boolean,
            usePaletteScale: Boolean,
            typography: WidgetTypographyPreset,
            maxEntries: Int,
            maxLabelChars: Int,
            isEn: Boolean,
        ): FlowRender {
            val entries = mutableListOf<FlowEntry>()
            var previousVisible: com.zilagent.app.data.entity.BellSchedule? = null
            items.forEach { item ->
                previousVisible?.let { previous ->
                    buildGapEntry(previous, item, all, isEn)?.let(entries::add)
                }
                var lessonNumber: Int? = null
                var classColor: Int? = null
                var classLabel = ""
                var subjectLabel = ""
                val label = when {
                    item.isBreak -> normalize(item.name, isEn)
                    isCountableLesson(item) -> {
                        val lessonNo = lessonNo(all, item)
                        lessonNumber = lessonNo
                        val full = db.syllabusDao().getFullSyllabusEntrySync(profileId, day, lessonNo)
                        classColor = parseColorOrNull(full?.classColor)
                        classLabel = full?.className?.trim().orEmpty()
                        subjectLabel = resolveLessonSubjectLabel(fallbackName = item.name, full = full, isEn = isEn)
                        buildCompactLessonLabel(lessonNo, item.name, full, showClassColors, isEn)
                    }
                    else -> normalize(item.name, isEn)
                }
                entries += FlowEntry(
                    startTime = item.startTime,
                    endTime = item.endTime,
                    label = label,
                    isBreak = item.isBreak,
                    isEmptyLesson = isEmptyLessonLabel(subjectLabel.ifBlank { label }, isEn),
                    isActive = activeId != null && item.id == activeId,
                    lessonNumber = lessonNumber,
                    classColor = classColor,
                    classLabel = classLabel,
                    subjectLabel = subjectLabel.ifBlank { label },
                )
                previousVisible = item
            }

            val visibleEntries = trimTrailingGapEntries(entries, isEn)
            val builder = SpannableStringBuilder()
            val entriesToRender = visibleEntries.take(maxEntries)
            val lessonWidth = entriesToRender
                .maxOfOrNull { it.lessonNumber?.let { number -> "$number.".length } ?: 0 }
                ?.coerceAtLeast(2)
                ?: 2
            val classWidth = entriesToRender
                .maxOfOrNull { compactClassLabel(it.classLabel).length }
                ?.coerceIn(0, 5)
                ?: 0
            val prefixWidth =
                lessonWidth +
                    (if (showTimes) 2 + 5 else 0) +
                    (if (classWidth > 0) 2 + classWidth else 0) +
                    1
            val totalRowWidth = maxLabelChars.coerceAtLeast(prefixWidth + 12)
            val subjectWidth = (totalRowWidth - prefixWidth).coerceAtLeast(12)
            entriesToRender
                .forEachIndexed { index, entry ->
                    val line = buildFlowRow(
                        entry = entry,
                        showIcons = showIcons,
                        showTimes = showTimes,
                        lessonWidth = lessonWidth,
                        classWidth = classWidth,
                        subjectWidth = subjectWidth,
                    )
                    val lineColor = when {
                        entry.isGap || entry.isEmptyLesson -> freePeriodColor(palette, gapColor)
                        entry.isBreak -> breakFlowColor(palette, baseColor)
                        showClassColors && entry.classColor != null -> harmonizedClassColor(
                            rawColor = entry.classColor,
                            palette = palette,
                            index = index,
                            usePaletteScale = usePaletteScale,
                        )
                        else -> neutralFlowColor(
                            index = index,
                            palette = palette,
                            fallback = baseColor,
                            usePaletteScale = usePaletteScale,
                        )
                    }
                    var depthColor = applyFlowDepth(
                        color = lineColor,
                        index = index,
                        isSpecial = entry.isGap || entry.isEmptyLesson || entry.isBreak,
                    )
                    if (entry.isActive) {
                        depthColor = activeFlowColor(
                            entry = entry,
                            palette = palette,
                            fallback = depthColor,
                            showClassColors = showClassColors,
                            usePaletteScale = usePaletteScale,
                        )
                    }
                    val spanLine = SpannableString(line).apply {
                        applyTypographySpan(this, typography)
                        setSpan(ForegroundColorSpan(depthColor), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        if (entry.isActive) {
                            applyActiveHighlight(this, palette, highlightStyle)
                        } else if (index == 0 || typography == WidgetTypographyPreset.Strong) {
                            setSpan(android.text.style.StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                    builder.append(spanLine)
                    if (index < entriesToRender.lastIndex) {
                        builder.append('\n')
                    }
                }
            return FlowRender(
                text = builder,
            )
        }

        private fun trimTrailingGapEntries(entries: List<FlowEntry>, isEn: Boolean): List<FlowEntry> {
            val trailingGapLabel = if (isEn) FREE_PERIOD_EN else FREE_PERIOD_TR
            val cleaned = entries.toMutableList()
            while (cleaned.size > 1) {
                val last = cleaned.last()
                if (!last.isGap && !last.isEmptyLesson && !last.label.equals(trailingGapLabel, ignoreCase = true)) break
                cleaned.removeAt(cleaned.lastIndex)
            }
            return cleaned
        }

        private fun trimPrimaryItem(
            items: List<com.zilagent.app.data.entity.BellSchedule>,
            primary: com.zilagent.app.data.entity.BellSchedule?,
        ): List<com.zilagent.app.data.entity.BellSchedule> {
            if (items.size <= 1) return items
            if (primary == null) return items.drop(1)
            return if (items.firstOrNull()?.id == primary.id) items.drop(1) else items
        }

        private fun flowLineBudget(flowElementSize: Int, density: WidgetInfoDensity): Int {
            val base = when {
                flowElementSize >= 32 -> 7
                flowElementSize >= 26 -> 9
                flowElementSize >= 20 -> 11
                else -> 13
            }
            return when (density) {
                WidgetInfoDensity.Sparse -> (base - 2).coerceAtLeast(4)
                WidgetInfoDensity.Balanced -> base
                WidgetInfoDensity.Dense -> base + 2
            }
        }

        private fun flowLabelBudget(
            flowElementSize: Int,
            showTimes: Boolean,
            density: WidgetInfoDensity,
            typography: WidgetTypographyPreset,
        ): Int {
            val base = when {
                flowElementSize >= 32 -> if (showTimes) 28 else 34
                flowElementSize >= 26 -> if (showTimes) 33 else 40
                flowElementSize >= 20 -> if (showTimes) 38 else 46
                else -> if (showTimes) 44 else 54
            }
            val densityBoost = when (density) {
                WidgetInfoDensity.Sparse -> -3
                WidgetInfoDensity.Balanced -> 0
                WidgetInfoDensity.Dense -> 4
            }
            val typographyBoost = when (typography) {
                WidgetTypographyPreset.Soft -> -1
                WidgetTypographyPreset.Strong -> 0
                WidgetTypographyPreset.Technical -> 3
                WidgetTypographyPreset.Notebook -> 1
            }
            return (base + densityBoost + typographyBoost).coerceAtLeast(20)
        }

        private suspend fun findNextDayFlow(
            db: AppDatabase,
            profileId: Long,
            startDay: Int,
            showBreaks: Boolean,
        ): DayFlow? {
            for (offset in 1..7) {
                val candidateDay = ((startDay - 1 + offset) % 7) + 1
                val all = db.bellDao().getSchedulesForProfileSync(profileId, candidateDay).sortedBy { it.startTime }
                if (all.isEmpty()) continue
                val visible = if (showBreaks) all else all.filter { !it.isBreak }
                if (visible.isNotEmpty()) {
                    return DayFlow(
                        profileId = profileId,
                        dayValue = candidateDay,
                        offset = offset,
                        all = all,
                        visible = visible,
                    )
                }
            }
            return null
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
            val targetId = ids.getOrNull(prefs.position.coerceIn(0, ids.lastIndex)) ?: return
            views.setViewVisibility(targetId, View.VISIBLE)
            views.setTextViewText(targetId, text)
            views.setTextColor(targetId, color)
        }

        private fun neutralFlowColor(
            index: Int,
            palette: WidgetPalette,
            fallback: Int,
            usePaletteScale: Boolean,
        ): Int {
            if (!usePaletteScale) return fallback
            return when (index % 4) {
                0 -> blendColors(palette.accent, palette.text, 0.46f)
                1 -> blendColors(palette.success, palette.text, 0.52f)
                2 -> blendColors(palette.footer, palette.text, 0.38f)
                else -> blendColors(palette.accent, palette.success, 0.58f)
            }
        }

        private fun flowGapColor(palette: WidgetPalette, fallback: Int): Int {
            return blendColors(palette.warning, fallback, 0.18f)
        }

        private fun harmonizedClassColor(
            rawColor: Int,
            palette: WidgetPalette,
            index: Int,
            usePaletteScale: Boolean,
        ): Int {
            if (!usePaletteScale) return rawColor
            val seed = when {
                android.graphics.Color.red(rawColor) >= android.graphics.Color.green(rawColor) &&
                    android.graphics.Color.red(rawColor) >= android.graphics.Color.blue(rawColor) -> palette.warning
                android.graphics.Color.green(rawColor) >= android.graphics.Color.red(rawColor) &&
                    android.graphics.Color.green(rawColor) >= android.graphics.Color.blue(rawColor) -> palette.success
                else -> palette.accent
            }
            val themed = blendColors(rawColor, seed, 0.44f)
            return blendColors(themed, neutralFlowColor(index, palette, palette.footer, true), 0.22f)
        }

        private fun activeFlowColor(
            entry: FlowEntry,
            palette: WidgetPalette,
            fallback: Int,
            showClassColors: Boolean,
            usePaletteScale: Boolean,
        ): Int {
            return when {
                showClassColors && entry.classColor != null -> harmonizedClassColor(
                    rawColor = entry.classColor,
                    palette = palette,
                    index = 0,
                    usePaletteScale = usePaletteScale,
                )
                entry.isGap || entry.isEmptyLesson -> blendColors(palette.warning, fallback, 0.08f)
                entry.isBreak -> blendColors(palette.warning, palette.text, 0.34f)
                else -> blendColors(palette.success, palette.text, 0.28f)
            }
        }

        private fun applyActiveHighlight(
            text: SpannableString,
            palette: WidgetPalette,
            style: SyllabusActiveHighlightStyle,
        ) {
            text.setSpan(android.text.style.StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            when (style) {
                SyllabusActiveHighlightStyle.Bold -> Unit
                SyllabusActiveHighlightStyle.Accent -> {
                    text.setSpan(ForegroundColorSpan(palette.accent), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                SyllabusActiveHighlightStyle.Soft -> {
                    text.setSpan(
                        android.text.style.BackgroundColorSpan(colorWithAlpha(palette.accent, 0.18f)),
                        0,
                        text.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
                SyllabusActiveHighlightStyle.Strong -> {
                    text.setSpan(
                        android.text.style.BackgroundColorSpan(colorWithAlpha(palette.success, 0.28f)),
                        0,
                        text.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                    text.setSpan(ForegroundColorSpan(palette.text), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        private fun applyFlowDepth(color: Int, index: Int, isSpecial: Boolean): Int {
            val alpha = when {
                index == 0 -> 1f
                isSpecial -> 0.96f
                index == 1 -> 0.95f
                index == 2 -> 0.91f
                else -> 0.86f
            }
            return colorWithAlpha(color, alpha)
        }

        private fun subtleFooterColor(palette: WidgetPalette): Int {
            return colorWithAlpha(palette.mutedText, 0.76f)
        }

        private fun blendColors(first: Int, second: Int, amount: Float): Int {
            val ratio = amount.coerceIn(0f, 1f)
            val inverse = 1f - ratio
            val a = ((android.graphics.Color.alpha(first) * inverse) + (android.graphics.Color.alpha(second) * ratio)).roundToInt()
            val r = ((android.graphics.Color.red(first) * inverse) + (android.graphics.Color.red(second) * ratio)).roundToInt()
            val g = ((android.graphics.Color.green(first) * inverse) + (android.graphics.Color.green(second) * ratio)).roundToInt()
            val b = ((android.graphics.Color.blue(first) * inverse) + (android.graphics.Color.blue(second) * ratio)).roundToInt()
            return android.graphics.Color.argb(a, r, g, b)
        }

        private fun lessonNo(all: List<com.zilagent.app.data.entity.BellSchedule>, item: com.zilagent.app.data.entity.BellSchedule): Int {
            return all.filter { isCountableLesson(it) && it.startTime <= item.startTime }.size
        }

        private fun isCountableLesson(item: com.zilagent.app.data.entity.BellSchedule): Boolean {
            if (item.isBreak) return false
            val lowered = item.name.lowercase()
            return "ders" in lowered || "lesson" in lowered
        }

        private fun buildLessonLabel(
            lessonNo: Int,
            fallbackName: String,
            full: com.zilagent.app.data.dao.SyllabusFullInfo?,
            showClassColors: Boolean,
            isEn: Boolean,
        ): String {
            val className = full?.className?.trim().orEmpty()
            val subjectName = full?.subjectName?.trim().orEmpty()
            val marker = if (showClassColors && !full?.classColor.isNullOrBlank()) "• " else ""
            val label = if (isEn) "Lesson $lessonNo" else "$lessonNo. ders"
            val normalizedFallback = normalize(fallbackName, isEn)
            return when {
                className.isNotEmpty() && subjectName.isNotEmpty() -> "$marker$label • $className - $subjectName"
                className.isNotEmpty() -> "$marker$label • $className"
                subjectName.isNotEmpty() -> "$marker$label • $subjectName"
                looksLikeLessonLabel(normalizedFallback, isEn) -> normalizedFallback
                else -> "$label • $normalizedFallback"
            }
        }

        private fun buildGapEntry(
            previous: com.zilagent.app.data.entity.BellSchedule,
            current: com.zilagent.app.data.entity.BellSchedule,
            all: List<com.zilagent.app.data.entity.BellSchedule>,
            isEn: Boolean,
        ): FlowEntry? {
            val previousIndex = all.indexOfFirst { it.id == previous.id }
            val currentIndex = all.indexOfFirst { it.id == current.id }
            if (previousIndex == -1 || currentIndex != previousIndex + 1) return null
            if (previous.isBreak || current.isBreak) return null
            if (!isCountableLesson(previous) || !isCountableLesson(current)) return null
            if (previous.endTime >= current.startTime) return null
            return FlowEntry(
                startTime = previous.endTime,
                endTime = current.startTime,
                label = if (isEn) FREE_PERIOD_EN else FREE_PERIOD_TR,
                isBreak = false,
                isGap = true,
            )
        }

        private fun looksLikeLessonLabel(value: String, isEn: Boolean): Boolean {
            val text = value.trim()
            return if (isEn) {
                Regex("""^(lesson\s+\d+|\d+\.?\s*lesson)\b""", RegexOption.IGNORE_CASE).containsMatchIn(text)
            } else {
                Regex("""^\d+\.?\s*ders\b""", RegexOption.IGNORE_CASE).containsMatchIn(text)
            }
        }

        private fun buildCompactLessonLabel(
            lessonNo: Int,
            fallbackName: String,
            full: com.zilagent.app.data.dao.SyllabusFullInfo?,
            _showClassColors: Boolean,
            isEn: Boolean,
        ): String {
            val className = full?.className?.trim().orEmpty()
            val subjectName = full?.subjectName?.trim().orEmpty()
            val normalizedFallback = normalize(fallbackName, isEn)
            val includeClass = className.isNotBlank() || (_showClassColors && className.isNotBlank())
            val fallbackLabel = when {
                looksLikeLessonLabel(normalizedFallback, isEn) -> ""
                normalizedFallback.isNotBlank() -> normalizedFallback
                else -> ""
            }
            val subjectOrFallback = subjectName.ifBlank { fallbackLabel }
            return when {
                includeClass && subjectOrFallback.isNotBlank() -> "$className $subjectOrFallback"
                includeClass -> className
                subjectOrFallback.isNotBlank() -> subjectOrFallback
                looksLikeLessonLabel(normalizedFallback, isEn) -> if (isEn) FREE_PERIOD_EN else FREE_PERIOD_TR
                else -> if (isEn) "Lesson $lessonNo" else "$lessonNo. ders"
            }
        }

        private fun resolveLessonSubjectLabel(
            fallbackName: String,
            full: com.zilagent.app.data.dao.SyllabusFullInfo?,
            isEn: Boolean,
        ): String {
            val subjectName = full?.subjectName?.trim().orEmpty()
            val normalizedFallback = normalize(fallbackName, isEn)
            return when {
                subjectName.isNotBlank() -> subjectName
                looksLikeLessonLabel(normalizedFallback, isEn) -> if (isEn) FREE_PERIOD_EN else FREE_PERIOD_TR
                normalizedFallback.isNotBlank() -> normalizedFallback
                else -> if (isEn) "Lesson" else "Ders"
            }
        }

        private fun isEmptyLessonLabel(value: String, isEn: Boolean): Boolean {
            val normalized = value.trim()
            return if (isEn) {
                normalized.equals(FREE_PERIOD_EN, ignoreCase = true)
            } else {
                normalized.equals(FREE_PERIOD_TR, ignoreCase = true)
            }
        }

        private fun compactClassLabel(value: String): String {
            val compact = value.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
            return when {
                compact.length <= 5 -> compact
                compact.contains(' ') -> compact
                    .split(' ')
                    .filter { it.isNotBlank() }
                    .joinToString("") { it.first().uppercase() }
                    .take(5)
                else -> compact.take(5)
            }
        }

        private fun truncateFlowLabel(value: String, maxChars: Int): String {
            val compact = value.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
            if (compact.length <= maxChars) return compact
            val hardLimit = (maxChars - 3).coerceAtLeast(1)
            val provisional = compact.take(hardLimit).trimEnd()
            val softBreak = provisional.lastIndexOf(' ')
            val clipped = if (softBreak >= (hardLimit * 0.6f).toInt()) {
                provisional.take(softBreak).trimEnd()
            } else {
                provisional
            }
            return clipped.ifBlank { provisional } + "..."
        }

        private fun buildFlowRow(
            entry: FlowEntry,
            showIcons: Boolean,
            showTimes: Boolean,
            lessonWidth: Int,
            classWidth: Int,
            subjectWidth: Int,
        ): String {
            val numberColumn = entry.lessonNumber?.let { "$it." } ?: ""
            val timeColumn = if (showTimes) formatTime(entry.startTime) else ""
            val classColumn = compactClassLabel(entry.classLabel)
            val hasClass = classColumn.isNotBlank()
            val subjectBase = buildString {
                if (showIcons) append(flowSymbol(entry))
                append(entry.subjectLabel.ifBlank { entry.label })
            }
            val subjectColumn = truncateFlowLabel(subjectBase, subjectWidth)

            return buildString {
                append(numberColumn.padEnd(lessonWidth, ' '))
                if (showTimes) {
                    append("  ")
                    append(timeColumn.padEnd(5, ' '))
                }
                if (classWidth > 0 && hasClass) {
                    append("  ")
                    append(classColumn.take(classWidth).padEnd(classWidth, ' '))
                }
                if (subjectColumn.isNotBlank()) {
                    append(' ')
                    append(subjectColumn)
                }
            }.trimEnd()
        }

        private fun flowSymbol(entry: FlowEntry): String {
            return when {
                entry.isGap || entry.isEmptyLesson -> "\u25CC "
                entry.isBreak -> "\u231B "
                entry.lessonNumber != null -> "\u270E "
                else -> "\u2726 "
            }
        }

        private fun flowIcon(entry: FlowEntry): String {
            return when {
                entry.isGap || entry.isEmptyLesson -> "▢ "
                entry.isBreak -> "⌛ "
                entry.lessonNumber != null -> "✎ "
                else -> "⚑ "
            }
        }

        private fun buildDayHeader(
            todayName: String,
            currentLesson: Int,
            totalLessons: Int,
            family: WidgetStyleFamily,
            isEn: Boolean,
        ): String {
            val progress = if (currentLesson > 0 && totalLessons > 0) " • $currentLesson/$totalLessons" else ""
            return when (family) {
                WidgetStyleFamily.Agenda -> if (isEn) "$todayName Agenda$progress" else "$todayName Ajanda$progress"
                WidgetStyleFamily.Minimal -> "$todayName$progress"
                WidgetStyleFamily.Energetic -> if (isEn) "$todayName • On flow$progress" else "$todayName • Ak\u0131\u015Fta$progress"
                else -> "$todayName$progress"
            }
        }

        private fun buildStatusLine(
            kind: String,
            body: String,
            family: WidgetStyleFamily,
            showIcons: Boolean,
            isEn: Boolean,
        ): String {
            val badge = statusBadge(kind, family, showIcons, isEn)
            val cleanBody = normalizeSeparators(body).trim()
            return if (cleanBody.isBlank()) badge else "$badge • $cleanBody"
        }

        private fun statusBadge(
            kind: String,
            family: WidgetStyleFamily,
            showIcons: Boolean,
            isEn: Boolean,
        ): String {
            val icon = if (showIcons) {
                when (kind) {
                    "now" -> "\u25B6 "
                    "next" -> "\u2192 "
                    "break" -> "\u231B "
                    "free" -> "\u25CC "
                    "holiday" -> "\u2726 "
                    "done" -> "\u2713 "
                    else -> ""
                }
            } else {
                ""
            }
            val label = when (kind) {
                "now" -> if (isEn) "Now" else "\u015Eimdi"
                "next" -> if (isEn) "Next" else "Sonra"
                "break" -> if (isEn) "Break" else "Teneff\u00FCs"
                "free" -> if (isEn) "Free" else "Bo\u015F"
                "holiday" -> if (isEn) "Holiday" else "Tatil"
                "done" -> if (isEn) "Day End" else "G\u00FCn Sonu"
                else -> if (isEn) "Status" else "Durum"
            }
            return when (family) {
                WidgetStyleFamily.Minimal -> icon + label.uppercase()
                WidgetStyleFamily.Agenda -> icon + if (isEn) "Plan: $label" else "Plan: $label"
                WidgetStyleFamily.Energetic -> icon + label.uppercase()
                else -> icon + label
            }.trim()
        }

        private fun extractStatusBody(raw: String): String {
            val normalized = normalizeSeparators(raw)
            val parts = normalized.split("•", limit = 2)
            return if (parts.size == 2) parts[1].trim() else normalized.trim()
        }

        private fun buildFooterLine(
            primary: String,
            family: WidgetStyleFamily,
            density: WidgetInfoDensity,
            isEn: Boolean,
        ): String {
            val base = normalizeSeparators(primary)
            return when (family) {
                WidgetStyleFamily.Agenda -> if (isEn) "Planner note • $base" else "Ajanda notu • $base"
                WidgetStyleFamily.Minimal -> base.substringBefore("•").trim()
                WidgetStyleFamily.Energetic -> if (density == WidgetInfoDensity.Dense) {
                    if (isEn) "Keep going • $base" else "Devam et • $base"
                } else {
                    base
                }
                else -> base
            }
        }

        private fun normalizeSeparators(raw: String): String {
            return raw.replace("â€¢", "•").replace(Regex("\\s+"), " ").trim()
        }

        private fun styledStaticText(
            text: String,
            typography: WidgetTypographyPreset,
            emphasize: Boolean,
        ): CharSequence {
            if (text.isBlank()) return text
            return SpannableString(text).apply {
                applyTypographySpan(this, typography)
                if (emphasize || typography == WidgetTypographyPreset.Strong) {
                    setSpan(android.text.style.StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        private fun styledFlowText(
            text: CharSequence,
            typography: WidgetTypographyPreset,
        ): CharSequence {
            if (text.isBlank()) return text
            return SpannableStringBuilder(text).apply {
                setSpan(TypefaceSpan(typefaceName(typography)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        private fun applyTypographySpan(
            text: SpannableString,
            typography: WidgetTypographyPreset,
        ) {
            text.setSpan(TypefaceSpan(typefaceName(typography)), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (typography == WidgetTypographyPreset.Soft) {
                text.setSpan(android.text.style.StyleSpan(Typeface.NORMAL), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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

        private fun freePeriodColor(palette: WidgetPalette, fallback: Int): Int {
            return blendColors(palette.warning, blendColors(palette.footer, fallback, 0.35f), 0.14f)
        }

        private fun breakFlowColor(palette: WidgetPalette, fallback: Int): Int {
            return blendColors(palette.accent, fallback, 0.26f)
        }

        private fun parseColorOrNull(colorHex: String?): Int? {
            if (colorHex.isNullOrBlank()) return null
            return runCatching { android.graphics.Color.parseColor(colorHex) }.getOrNull()
        }

        private fun colorWithAlpha(color: Int, alpha: Float): Int {
            val safeAlpha = (alpha.coerceIn(0f, 1f) * 255f).toInt()
            return (color and 0x00FFFFFF) or (safeAlpha shl 24)
        }

        private fun normalize(raw: String, isEn: Boolean): String {
            val cleaned = raw
                .replace("Giriş", "Bitiş")
                .replace("Giris", "Bitiş")
                .replace("GiriÅŸ", "Bitiş")
                .replace("Teneffus", "Teneffüs")
                .replace("TeneffÃ¼s", "Teneffüs")
                .replace("Ogle Arasi", "Öğle Arası")
                .replace("Ã–ÄŸle ArasÄ±", "Öğle Arası")
            return if (isEn) {
                cleaned
                    .replace("Bitiş", "Ends")
                    .replace("Teneffüs", "Break")
                    .replace("Öğle Arası", "Lunch Break")
                    .replace("Ders", "Lesson")
            } else {
                cleaned
            }
        }

        private fun buildPreviewDayLabel(
            dayValue: Int,
            offset: Int,
            family: WidgetStyleFamily,
            isEn: Boolean,
        ): String {
            return when (family) {
                WidgetStyleFamily.Agenda -> {
                    val day = dayName(dayValue, isEn)
                    if (isEn) "$day • Planner" else "$day • Plan"
                }
                WidgetStyleFamily.Minimal -> dayName(dayValue, isEn)
                else -> buildPreviewDayLabel(dayValue, offset, isEn)
            }
        }

        private fun buildPreviewDayLabel(dayValue: Int, offset: Int, isEn: Boolean): String {
            val day = dayName(dayValue, isEn)
            val suffix = when {
                offset == 1 && isEn -> "Tomorrow"
                offset == 1 -> "Yarın"
                isEn -> "Upcoming"
                else -> "Yaklaşan"
            }
            return "$day • $suffix"
        }

        private fun dayName(dayValue: Int, isEn: Boolean): String {
            return when (dayValue) {
                1 -> if (isEn) "Monday" else "Pazartesi"
                2 -> if (isEn) "Tuesday" else "Salı"
                3 -> if (isEn) "Wednesday" else "Çarşamba"
                4 -> if (isEn) "Thursday" else "Perşembe"
                5 -> if (isEn) "Friday" else "Cuma"
                6 -> if (isEn) "Saturday" else "Cumartesi"
                7 -> if (isEn) "Sunday" else "Pazar"
                else -> if (isEn) "Today" else "Bugün"
            }
        }

        private fun hideAll(views: RemoteViews, ids: IntArray) {
            ids.forEach { viewId -> views.setViewVisibility(viewId, View.GONE) }
        }

        private fun applyTextSize(views: RemoteViews, ids: IntArray, sizeSp: Float) {
            ids.toSet().forEach { viewId ->
                views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, sizeSp)
            }
        }

        private fun syllabusElementSizeSp(context: Context, element: SyllabusWidgetElement): Float {
            val slider = WidgetStore.getSyllabusElementSize(context, element)
            val base = when (element) {
                SyllabusWidgetElement.Day -> 11f + (slider * 0.34f)
                SyllabusWidgetElement.Status -> 10f + (slider * 0.30f)
                SyllabusWidgetElement.Flow -> 9f + (slider * 0.34f)
                SyllabusWidgetElement.Footer -> 9f + (slider * 0.24f)
            }
            val globalScale = WidgetStore.getGlobalWidgetTextScale(context) / 100f
            return (base * globalScale).coerceIn(8f, 72f)
        }

        private fun formatTime(mins: Int): String {
            val h = mins / 60
            val m = mins % 60
            return "%02d:%02d".format(h, m)
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, SyllabusWidget::class.java))
            CoroutineScope(Dispatchers.IO).launch {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                runCatching { com.zilagent.app.manager.BellManager(context).scheduleMinuteTick() }
            }
        }
    }
}
