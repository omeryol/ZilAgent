package com.zilagent.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.zilagent.app.MainActivity
import com.zilagent.app.R
import java.time.LocalTime

class PanoramicCountdownWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_dynamic_h)
            val isEn = WidgetStore.getAppLanguage(context) == "en"
            fun t(tr: String, en: String): String = if (isEn) en else tr

            val (customEnabled, customTitle, customTime) = WidgetStore.getCustomCountdown(context)
            val endTimeMinutes: Int
            val bellName: String
            if (customEnabled && customTime != -1) {
                endTimeMinutes = customTime
                bellName = customTitle.ifEmpty { t("Özel sayaç", "Custom timer") }
            } else {
                bellName = WidgetStore.getNextBellName(context) ?: t("Bekleniyor", "Waiting")
                endTimeMinutes = WidgetStore.getNextBellTime(context)
            }

            val normalizedTitle = normalize(bellName, isEn)
            val stage = normalizedTitle.substringBefore("•").trim().ifBlank { normalizedTitle }
            val (currentLesson, totalLessons, isBreak) = WidgetStore.getLessonProgress(context)
            val timeTextSize = WidgetStore.getPanoramicTimeTextSize(context).toFloat()
            val titleTextSize = WidgetStore.getPanoramicTitleTextSize(context).toFloat()
            val showProgress = WidgetStore.isProgressBarEnabled(context)

            views.setTextViewText(R.id.widget_title, normalizedTitle)
            views.setTextViewText(R.id.widget_badge, if (customEnabled) t("SINAV MODU", "EXAM MODE") else t("SIRADAKİ ZİL", "NEXT BELL"))
            views.setTextViewTextSize(R.id.widget_chronometer, TypedValue.COMPLEX_UNIT_SP, timeTextSize)
            views.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, titleTextSize)
            views.setTextViewTextSize(R.id.widget_meta, TypedValue.COMPLEX_UNIT_SP, (titleTextSize - 3f).coerceAtLeast(11f))

            if (endTimeMinutes != -1) {
                val now = LocalTime.now()
                val secondsDiff = (endTimeMinutes * 60 - now.toSecondOfDay()).toLong()
                if (secondsDiff > 0) {
                    if (WidgetStore.isShowSeconds(context)) {
                        val baseTime = SystemClock.elapsedRealtime() + (secondsDiff * 1000)
                        views.setChronometer(R.id.widget_chronometer, baseTime, "%s", true)
                        views.setChronometerCountDown(R.id.widget_chronometer, true)
                        views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
                    } else {
                        views.setTextViewText(R.id.widget_chronometer, formatDuration(secondsDiff))
                        views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
                    }
                    val progressMeta = if (currentLesson > 0 && totalLessons > 0) {
                        if (isBreak) {
                            if (isEn) "$currentLesson/$totalLessons break" else "$currentLesson/$totalLessons teneffüs"
                        } else {
                            if (isEn) "$currentLesson/$totalLessons lesson" else "$currentLesson/$totalLessons ders"
                        }
                    } else {
                        stage
                    }
                    views.setTextViewText(R.id.widget_meta, "${t("Durum", "Status")}: $progressMeta")
                } else {
                    views.setTextViewText(R.id.widget_chronometer, "00:00")
                    views.setTextViewText(R.id.widget_meta, t("Süre doldu", "Time is up"))
                }
            } else {
                views.setTextViewText(R.id.widget_chronometer, "--:--")
                views.setTextViewText(R.id.widget_meta, t("Bugün ders yok", "No lessons today"))
            }

            views.setViewVisibility(R.id.widget_progress_bar, if (showProgress) View.VISIBLE else View.GONE)
            if (showProgress) {
                val (startMinutes, endMinutes) = WidgetStore.getCurrentEventTimes(context)
                val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
                if (startMinutes != -1 && endMinutes != -1 && nowMinutes in startMinutes..endMinutes) {
                    val totalDuration = endMinutes - startMinutes
                    val elapsed = nowMinutes - startMinutes
                    val progress = if (totalDuration > 0) {
                        ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt().coerceIn(0, 100)
                    } else {
                        0
                    }
                    views.setProgressBar(R.id.widget_progress_bar, 100, progress, false)
                } else {
                    views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
                }
            } else {
                views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
            }

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

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun normalize(raw: String, isEn: Boolean): String {
            val tr = raw
                .replace("Giriş", "Bitiş")
                .replace("Giris", "Bitiş")
            return if (isEn) {
                tr.replace("Bitiş", "Ends")
                    .replace("Ders", "Lesson")
                    .replace("Teneffüs", "Break")
            } else {
                tr
            }
        }

        private fun formatDuration(totalSeconds: Long): String {
            val minutes = (totalSeconds / 60).coerceAtLeast(0)
            val seconds = (totalSeconds % 60).coerceAtLeast(0)
            return "%02d:%02d".format(minutes, seconds)
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, PanoramicCountdownWidget::class.java))
            for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
