package com.zilagent.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.widget.RemoteViews
import com.zilagent.app.MainActivity
import com.zilagent.app.R
import com.zilagent.app.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar

class SyllabusWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_syllabus)
            val isEn = WidgetStore.getAppLanguage(context) == "en"
            fun t(tr: String, en: String): String = if (isEn) en else tr

            val nowCal = Calendar.getInstance()
            val nowMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
            val dayName = dayName(nowCal.get(Calendar.DAY_OF_WEEK), isEn)
            val flowTextSize = WidgetStore.getSyllabusFlowTextSize(context).toFloat()
            val statusTextSize = WidgetStore.getSyllabusStatusTextSize(context).toFloat()
            val showIcons = WidgetStore.isSyllabusShowIcons(context)
            val showClassColors = WidgetStore.isSyllabusShowClassColors(context)
            val showBreaks = WidgetStore.isSyllabusShowBreaks(context)
            val showTimes = WidgetStore.isSyllabusShowTimes(context)
            val colorizeText = WidgetStore.isSyllabusColorizeText(context)
            val progress = WidgetStore.getLessonProgress(context)
            views.setTextViewTextSize(R.id.widget_line1, TypedValue.COMPLEX_UNIT_SP, flowTextSize)
            views.setTextViewTextSize(R.id.widget_status, TypedValue.COMPLEX_UNIT_SP, statusTextSize)

            try {
                runBlocking {
                    val db = AppDatabase.getDatabase(context)
                    val profile = db.bellDao().getActiveProfileSync()
                    if (profile == null) {
                        views.setTextViewText(R.id.widget_day, dayName)
                        views.setTextViewText(R.id.widget_status, t("Aktif profil yok", "No active profile"))
                        views.setTextViewText(R.id.widget_line1, t("Ayarlar > Profil seç", "Settings > Select profile"))
                        views.setTextViewText(R.id.widget_line2, "")
                        views.setTextViewText(R.id.widget_line3, "")
                    } else {
                        val day = LocalDate.now().dayOfWeek.value
                        val all = db.bellDao().getSchedulesForProfileSync(profile.id, day).sortedBy { it.startTime }
                        val remaining = all.filter { it.endTime > nowMinutes }
                        val active = all.firstOrNull { nowMinutes >= it.startTime && nowMinutes < it.endTime }
                        val remainingVisible = if (showBreaks) remaining else remaining.filter { !it.isBreak }
                        val activeVisible = if (!showBreaks && active?.isBreak == true) null else active

                        val currentLessonNo = when {
                            active == null -> {
                                val nextLesson = remaining.firstOrNull { isCountableLesson(it) }
                                if (nextLesson != null) lessonNo(all, nextLesson) else null
                            }
                            active.isBreak -> lessonNo(all, active)
                            else -> lessonNo(all, active)
                        }

                        val dayHeader = if (currentLessonNo != null) {
                            val p = if (progress.first > 0 && progress.second > 0) " (${progress.first}/${progress.second})" else ""
                            if (isEn) "$dayName • Lesson $currentLessonNo$p" else "$dayName • ${currentLessonNo}. ders$p"
                        } else {
                            dayName
                        }
                        views.setTextViewText(R.id.widget_day, dayHeader)

                        val status = when {
                            activeVisible == null && remainingVisible.isEmpty() -> t("Bugün dersler bitti", "Lessons are over for today")
                            activeVisible != null && activeVisible.isBreak -> t("Şu an", "Now") + ": ${normalize(activeVisible.name, isEn)}"
                            activeVisible != null -> {
                                val no = lessonNo(all, activeVisible)
                                val full = db.syllabusDao().getFullSyllabusEntrySync(profile.id, day, no)
                                t("Şu an", "Now") + ": ${buildLessonLabel(no, activeVisible.name, full, showClassColors, isEn)}"
                            }
                            else -> {
                                val next = remainingVisible.first()
                                if (next.isBreak) {
                                    t("Sonraki", "Next") + ": ${normalize(next.name, isEn)}"
                                } else {
                                    val no = lessonNo(all, next)
                                    val full = db.syllabusDao().getFullSyllabusEntrySync(profile.id, day, no)
                                    t("Sonraki", "Next") + ": ${buildLessonLabel(no, next.name, full, showClassColors, isEn)}"
                                }
                            }
                        }
                        views.setTextViewText(R.id.widget_status, status)
                        views.setTextColor(
                            R.id.widget_status,
                            if (colorizeText) {
                                if (activeVisible != null) 0xFF96E6B3.toInt() else 0xFFFFD166.toInt()
                            } else {
                                0xFFFFFFFF.toInt()
                            },
                        )

                        if (remainingVisible.isEmpty()) {
                            views.setTextViewText(R.id.widget_line1, t("Yarının akışı için uygulamayı aç", "Open the app for tomorrow's flow"))
                            views.setTextViewText(R.id.widget_line2, "")
                            views.setTextViewText(R.id.widget_line3, "")
                        } else {
                            val builder = SpannableStringBuilder()
                            val visibleLines = remainingVisible.take(12)
                            for ((index, item) in visibleLines.withIndex()) {
                                val fullForItem = if (!item.isBreak) db.syllabusDao().getFullSyllabusEntrySync(profile.id, day, lessonNo(all, item)) else null
                                val lineLabel = if (item.isBreak) {
                                    normalize(item.name, isEn)
                                } else {
                                    val no = lessonNo(all, item)
                                    buildLessonLabel(no, item.name, fullForItem, showClassColors, isEn)
                                }
                                val icon = if (showIcons) "${eventIcon(item.name)} " else ""
                                val timePart = if (showTimes) "${formatTime(item.startTime)}-${formatTime(item.endTime)}  " else ""
                                val lineText = "$icon$timePart$lineLabel"
                                val lineColor = if (!colorizeText) {
                                    0xFFFFFFFF.toInt()
                                } else if (item.isBreak) {
                                    0xFFFFC857.toInt()
                                } else {
                                    colorFromHexOrDefault(fullForItem?.classColor, 0xFF9AD0FF.toInt())
                                }
                                val span = SpannableString(lineText)
                                span.setSpan(ForegroundColorSpan(lineColor), 0, lineText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                builder.append(span)
                                if (index < visibleLines.size - 1) builder.append('\n')
                            }
                            views.setTextViewText(R.id.widget_line1, builder)
                            views.setTextViewText(R.id.widget_line2, "")
                            views.setTextViewText(R.id.widget_line3, "")
                        }
                    }
                }
            } catch (_: Exception) {
                views.setTextViewText(R.id.widget_day, dayName)
                views.setTextViewText(R.id.widget_status, t("Widget verisi okunamadı", "Widget data unavailable"))
                views.setTextViewText(R.id.widget_line1, t("Uygulamayı açıp tekrar dene", "Open app and try again"))
                views.setTextViewText(R.id.widget_line2, "")
                views.setTextViewText(R.id.widget_line3, "")
            }

            val appIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, SyllabusWidget::class.java))
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun lessonNo(all: List<com.zilagent.app.data.entity.BellSchedule>, item: com.zilagent.app.data.entity.BellSchedule): Int {
            return all.filter { isCountableLesson(it) && it.startTime <= item.startTime }.size.coerceAtLeast(1)
        }

        private fun isCountableLesson(item: com.zilagent.app.data.entity.BellSchedule): Boolean {
            if (item.isBreak) return false
            val n = item.name.lowercase()
            return "ders" in n || "lesson" in n
        }

        private fun normalize(raw: String, isEn: Boolean): String {
            val v = raw
                .replace("Giriş", "Bitiş")
                .replace("Giris", "Bitiş")
                .replace("Teneffus", "Teneffüs")
                .replace("Ogle Arasi", "Öğle Arası")
                .replace("Ogle", "Öğle")
                .replace("Arasi", "Arası")
                .replace("Bugun", "Bugün")
                .replace("Su an", "Şu an")
            return if (!isEn) v else v
                .replace("Bitiş", "Ends")
                .replace("Teneffüs", "Break")
                .replace("Öğle Arası", "Lunch Break")
                .replace("Öğle", "Lunch")
                .replace("Ders", "Lesson")
                .replace("Şu an", "Now")
                .replace("Bugün", "Today")
        }

        private fun eventIcon(name: String): String {
            val n = name.lowercase()
            return when {
                "teneff" in n || "break" in n -> "☕"
                "öğle" in n || "ogle" in n || "lunch" in n -> "🍽"
                "ders" in n || "lesson" in n -> "📘"
                else -> "•"
            }
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
            val colorDot = if (showClassColors) colorDot(full?.classColor) else ""
            val lessonLabel = if (isEn) "Lesson $lessonNo" else "$lessonNo. ders"

            return when {
                className.isNotEmpty() && subjectName.isNotEmpty() -> "$colorDot $lessonLabel • $className - $subjectName".trimStart()
                className.isNotEmpty() -> "$colorDot $lessonLabel • $className".trimStart()
                subjectName.isNotEmpty() -> "$colorDot $lessonLabel • $subjectName".trimStart()
                else -> "$lessonLabel • ${normalize(fallbackName, isEn)}"
            }
        }

        private fun colorDot(hex: String?): String {
            if (hex.isNullOrBlank()) return "⚪"
            return try {
                val color = Color.parseColor(hex)
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                when {
                    r > 200 && g < 120 && b < 120 -> "🔴"
                    r > 220 && g > 150 && b < 100 -> "🟠"
                    r > 200 && g > 200 && b < 120 -> "🟡"
                    g > 170 && r < 140 && b < 170 -> "🟢"
                    b > 170 && r < 150 -> "🔵"
                    r > 160 && b > 160 -> "🟣"
                    else -> "⚪"
                }
            } catch (_: Exception) {
                "⚪"
            }
        }

        private fun colorFromHexOrDefault(hex: String?, fallback: Int): Int {
            if (hex.isNullOrBlank()) return fallback
            return try {
                Color.parseColor(hex)
            } catch (_: Exception) {
                fallback
            }
        }

        private fun dayName(calendarDay: Int, isEn: Boolean): String {
            return when (calendarDay) {
                Calendar.MONDAY -> if (isEn) "Monday" else "Pazartesi"
                Calendar.TUESDAY -> if (isEn) "Tuesday" else "Salı"
                Calendar.WEDNESDAY -> if (isEn) "Wednesday" else "Çarşamba"
                Calendar.THURSDAY -> if (isEn) "Thursday" else "Perşembe"
                Calendar.FRIDAY -> if (isEn) "Friday" else "Cuma"
                Calendar.SATURDAY -> if (isEn) "Saturday" else "Cumartesi"
                Calendar.SUNDAY -> if (isEn) "Sunday" else "Pazar"
                else -> if (isEn) "Today" else "Bugün"
            }
        }

        private fun formatTime(mins: Int): String {
            val h = mins / 60
            val m = mins % 60
            return "%02d:%02d".format(h, m)
        }
    }
}
