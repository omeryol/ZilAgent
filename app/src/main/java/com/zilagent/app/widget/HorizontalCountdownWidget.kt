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

class HorizontalCountdownWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_countdown_horizontal)

            val (customEnabled, customTitle, customTime) = WidgetStore.getCustomCountdown(context)
            
            var bellTime: Int
            var bellName: String
            
            if (customEnabled && customTime != -1) {
                bellTime = customTime
                bellName = customTitle.ifEmpty { "Özel Sayaç" }
            } else {
                bellName = WidgetStore.getNextBellName(context) ?: "Yükleniyor..."
                bellTime = WidgetStore.getNextBellTime(context)
            }

            views.setTextViewText(R.id.widget_status, bellName)

            // --- Advanced Styling ---
            val textSize = WidgetStore.getWidgetTextSize(context)
            val labelSize = WidgetStore.getWidgetLabelTextSize(context)
            val opacity = WidgetStore.getWidgetBgOpacity(context)
            val textColorHex = WidgetStore.getWidgetTextColor(context)
            val bgColorHex = WidgetStore.getWidgetBgColor(context)
            val barThickness = WidgetStore.getWidgetBarThickness(context)

            val textColor = android.graphics.Color.parseColor(textColorHex)
            val baseBgColor = android.graphics.Color.parseColor(bgColorHex)
            val alpha = (opacity * 255) / 100
            val finalBgColor = android.graphics.Color.argb(alpha, android.graphics.Color.red(baseBgColor), android.graphics.Color.green(baseBgColor), android.graphics.Color.blue(baseBgColor))

            views.setInt(R.id.widget_root, "setBackgroundColor", finalBgColor)
            views.setTextColor(R.id.widget_status, textColor)
            views.setTextColor(R.id.widget_chronometer, textColor)
            views.setTextColor(R.id.widget_percentage, textColor)
            
            views.setTextViewTextSize(R.id.widget_chronometer, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
            views.setTextViewTextSize(R.id.widget_status, TypedValue.COMPLEX_UNIT_SP, labelSize.toFloat())
            views.setTextViewTextSize(R.id.widget_percentage, TypedValue.COMPLEX_UNIT_SP, (labelSize * 0.7).toFloat())
            
            val paddingV = (16 - barThickness).coerceAtLeast(0) / 2
            views.setViewPadding(R.id.widget_progress_bar, 0, paddingV, 0, paddingV)
            // -------------------------
            
            if (bellTime != -1) {
                val now = LocalTime.now()
                val nowMinutes = now.hour * 60 + now.minute
                val minutesDiff = bellTime - nowMinutes

                if (minutesDiff >= 0) {
                     val nowSecondsOfDay = now.toSecondOfDay()
                     val targetSecondsOfDay = bellTime * 60
                     val secondsDiff = targetSecondsOfDay - nowSecondsOfDay
                     
                     if (secondsDiff > 0) {
                        val baseTime = SystemClock.elapsedRealtime() + (secondsDiff * 1000)

                        // Pulse effect (Last 1 minute)
                        if (secondsDiff <= 60) {
                            views.setInt(R.id.widget_root, "setBackgroundColor", android.graphics.Color.argb(alpha, 255, 235, 238))
                        }

                        val isDynamicColor = WidgetStore.isDynamicColorEnabled(context)
                        if (isDynamicColor && minutesDiff <= 2) {
                            views.setTextColor(R.id.widget_chronometer, android.graphics.Color.RED)
                        }
    
                        views.setChronometer(R.id.widget_chronometer, baseTime, null, true)
                        views.setChronometerCountDown(R.id.widget_chronometer, true)
                        views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
                     } else {
                        views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                        views.setTextViewText(R.id.widget_status, "$bellName Çaldı!")
                     }
                } else {
                    views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                    views.setTextViewText(R.id.widget_status, "Sıradaki Bekleniyor...")
                }
            } else {
                 views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                 views.setTextViewText(R.id.widget_status, bellName)
            }
        
            // Click Intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Progress Bar Logic
            val (startMinutes, endMinutes) = WidgetStore.getCurrentEventTimes(context)
            val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute

            if (startMinutes != -1 && endMinutes != -1 && nowMinutes >= startMinutes && nowMinutes <= endMinutes) {
                val totalDuration = endMinutes - startMinutes
                val elapsed = nowMinutes - startMinutes
                
                if (totalDuration > 0) {
                     val progress = ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt()
                     val remaining = (100 - progress).coerceAtLeast(0)
                     
                     views.setProgressBar(R.id.widget_progress_bar, 100, progress, false)
                     views.setViewVisibility(R.id.widget_progress_bar, View.VISIBLE)
                     
                     views.setTextViewText(R.id.widget_percentage, "$remaining%")
                     views.setViewVisibility(R.id.widget_percentage, View.VISIBLE)
                } else {
                     views.setViewVisibility(R.id.widget_progress_bar, View.GONE)
                     views.setViewVisibility(R.id.widget_percentage, View.GONE)
                }
            } else {
                views.setViewVisibility(R.id.widget_progress_bar, View.GONE)
                views.setViewVisibility(R.id.widget_percentage, View.GONE)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        fun updateAll(context: Context) {
             val appWidgetManager = AppWidgetManager.getInstance(context)
             val thisWidget = ComponentName(context, HorizontalCountdownWidget::class.java)
             val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
             for (appWidgetId in appWidgetIds) {
                 updateAppWidget(context, appWidgetManager, appWidgetId)
             }
        }
    }
}
