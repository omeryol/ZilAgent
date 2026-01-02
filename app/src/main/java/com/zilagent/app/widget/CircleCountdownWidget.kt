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

class CircleCountdownWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_circle)
            
            val (customEnabled, customTitle, customTime) = WidgetStore.getCustomCountdown(context)
            var endTimeMinutes: Int
            var bellName: String
            
            if (customEnabled && customTime != -1) {
                endTimeMinutes = customTime
                bellName = customTitle.ifEmpty { "Özel" }
            } else {
                bellName = WidgetStore.getNextBellName(context) ?: "Bekleniyor"
                endTimeMinutes = WidgetStore.getNextBellTime(context)
            }

            // Styling
            val textSize = WidgetStore.getWidgetTextSize(context)
            val labelSize = WidgetStore.getWidgetLabelTextSize(context)
            val opacity = WidgetStore.getWidgetBgOpacity(context)
            val textColorHex = WidgetStore.getWidgetTextColor(context)
            val bgColorHex = WidgetStore.getWidgetBgColor(context)
            
            val textColor = android.graphics.Color.parseColor(textColorHex)
            val baseBgColor = android.graphics.Color.parseColor(bgColorHex)
            val alpha = (opacity * 255) / 100
            val finalBgColor = android.graphics.Color.argb(alpha, android.graphics.Color.red(baseBgColor), android.graphics.Color.green(baseBgColor), android.graphics.Color.blue(baseBgColor))

            // Background ring tint
            views.setInt(R.id.widget_bg_img, "setColorFilter", finalBgColor)
            views.setInt(R.id.widget_bg_img, "setImageAlpha", alpha)
            
            views.setTextColor(R.id.widget_title, textColor)
            views.setTextColor(R.id.widget_chronometer, textColor)
            
            views.setTextViewTextSize(R.id.widget_chronometer, TypedValue.COMPLEX_UNIT_SP, (textSize - 4).coerceAtLeast(16).toFloat())
            views.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, (labelSize - 2).coerceAtLeast(8).toFloat())
            
            if (endTimeMinutes != -1) {
                val now = LocalTime.now()
                val secondsDiff = (endTimeMinutes * 60 - now.toSecondOfDay()).toLong()
                
                if (secondsDiff > 0) {
                    if (WidgetStore.isShowSeconds(context)) {
                        val baseTime = SystemClock.elapsedRealtime() + (secondsDiff * 1000)
                        views.setChronometer(R.id.widget_chronometer, baseTime, null, true)
                        views.setChronometerCountDown(R.id.widget_chronometer, true)
                        views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
                    } else {
                        val minuteText = com.zilagent.app.util.TimeUtils.formatCountdown(secondsDiff, false)
                        views.setTextViewText(R.id.widget_chronometer, minuteText)
                        views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
                    }
                    val cleanTitle = bellName.split("\n").first().split(" • ").first()
                    views.setTextViewText(R.id.widget_title, cleanTitle)
                } else {
                    views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                    views.setTextViewText(R.id.widget_title, "Bitti")
                }
            } else {
                views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                views.setTextViewText(R.id.widget_title, bellName)
            }

            // Progress Bar Logic (Always visible for Circle widget if enabled)
            val (startMinutes, endMinutes) = WidgetStore.getCurrentEventTimes(context)
            val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
            val isProgressBarEnabled = WidgetStore.isProgressBarEnabled(context)

            if (isProgressBarEnabled) {
                views.setViewVisibility(R.id.widget_circular_progress, View.VISIBLE)
                // Use textColor for the progress ring so it matches the theme
                // RemoteViews doesn't support setProgressTintList easily, but we can tint the whole view?
                // Actually, the drawable is white, so it will stay white unless we tint it.
                // For now, let's just show it.
                
                if (startMinutes != -1 && endMinutes != -1 && nowMinutes >= startMinutes && nowMinutes <= endMinutes) {
                    val totalDuration = endMinutes - startMinutes
                    val elapsed = nowMinutes - startMinutes
                    if (totalDuration > 0) {
                        val progress = ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt()
                        views.setProgressBar(R.id.widget_circular_progress, 100, progress, false)
                    } else {
                        views.setProgressBar(R.id.widget_circular_progress, 100, 100, false)
                    }
                } else {
                    views.setProgressBar(R.id.widget_circular_progress, 100, 0, false)
                }
            } else {
                views.setViewVisibility(R.id.widget_circular_progress, View.GONE)
            }

            val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CircleCountdownWidget::class.java))
            for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
