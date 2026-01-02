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

class ModernCountdownWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val flow = WidgetStore.getWidgetFlowDirection(context)
            val order = WidgetStore.getWidgetElementOrder(context)
            
            val layoutId = if (flow == 0) {
                if (order == 0) R.layout.widget_dynamic_v else R.layout.widget_dynamic_v_alt
            } else {
                if (order == 0) R.layout.widget_dynamic_h else R.layout.widget_dynamic_h_alt
            }
            
            val views = RemoteViews(context.packageName, layoutId)

            val alignment = WidgetStore.getWidgetAlignment(context)
            val spacing = WidgetStore.getWidgetSpacing(context)
            
            val gravity = when(alignment) {
                0 -> android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
                2 -> android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
                else -> android.view.Gravity.CENTER
            }
            views.setInt(R.id.content_block, "setGravity", gravity)
            
            val contentGravity = when(alignment) {
                0 -> android.view.Gravity.START
                2 -> android.view.Gravity.END
                else -> android.view.Gravity.CENTER_HORIZONTAL
            }
            if (flow == 0) views.setInt(R.id.content_block, "setGravity", contentGravity)
            views.setInt(R.id.widget_title, "setGravity", contentGravity)

            val density = context.resources.displayMetrics.density
            val spacingPx = (spacing * density).toInt()
            
            val secondElementId = if (order == 0) R.id.widget_title else R.id.widget_chronometer
            val firstElementId = if (order == 0) R.id.widget_chronometer else R.id.widget_title
            
            views.setViewLayoutMargin(firstElementId, RemoteViews.MARGIN_TOP, 0f, TypedValue.COMPLEX_UNIT_PX)
            views.setViewLayoutMargin(firstElementId, RemoteViews.MARGIN_START, 0f, TypedValue.COMPLEX_UNIT_PX)
            views.setViewLayoutMargin(secondElementId, RemoteViews.MARGIN_TOP, 0f, TypedValue.COMPLEX_UNIT_PX)
            views.setViewLayoutMargin(secondElementId, RemoteViews.MARGIN_START, 0f, TypedValue.COMPLEX_UNIT_PX)

            if (flow == 0) {
                views.setViewLayoutMargin(secondElementId, RemoteViews.MARGIN_TOP, spacingPx.toFloat(), TypedValue.COMPLEX_UNIT_PX)
            } else {
                views.setViewLayoutMargin(secondElementId, RemoteViews.MARGIN_START, spacingPx.toFloat(), TypedValue.COMPLEX_UNIT_PX)
            }

            val (customEnabled, customTitle, customTime) = WidgetStore.getCustomCountdown(context)
            var endTimeMinutes: Int
            var bellName: String
            
            if (customEnabled && customTime != -1) {
                endTimeMinutes = customTime
                bellName = customTitle.ifEmpty { "Özel Sayaç" }
            } else {
                bellName = WidgetStore.getNextBellName(context) ?: "Bekleniyor..."
                endTimeMinutes = WidgetStore.getNextBellTime(context)
            }

            val textSize = WidgetStore.getWidgetTextSize(context)
            val labelSize = WidgetStore.getWidgetLabelTextSize(context)
            val opacity = WidgetStore.getWidgetBgOpacity(context)
            val textColorHex = WidgetStore.getWidgetTextColor(context)
            val bgColorHex = WidgetStore.getWidgetBgColor(context)
            
            val textColor = android.graphics.Color.parseColor(textColorHex)
            val baseBgColor = android.graphics.Color.parseColor(bgColorHex)
            val alpha = (opacity * 255) / 100
            val finalBgColor = android.graphics.Color.argb(alpha, android.graphics.Color.red(baseBgColor), android.graphics.Color.green(baseBgColor), android.graphics.Color.blue(baseBgColor))

            views.setInt(R.id.widget_root, "setBackgroundColor", finalBgColor)
            views.setTextColor(R.id.widget_title, textColor)
            views.setTextColor(R.id.widget_chronometer, textColor)
            views.setTextColor(R.id.widget_percentage, textColor)
            
            views.setTextViewTextSize(R.id.widget_chronometer, TypedValue.COMPLEX_UNIT_SP, (textSize + 4).toFloat())
            views.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, labelSize.toFloat())
            views.setTextViewTextSize(R.id.widget_percentage, TypedValue.COMPLEX_UNIT_SP, (labelSize * 0.8).toFloat())
            
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
                        views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                        views.setTextViewText(R.id.widget_chronometer, minuteText)
                        views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
                    }
                    
                    val isMultiline = WidgetStore.isMultilineEnabled(context)
                    val labelText = if (isMultiline) {
                        views.setInt(R.id.widget_title, "setMaxLines", 2)
                        bellName.replace(" • ", "\n")
                    } else {
                        views.setInt(R.id.widget_title, "setMaxLines", 1)
                        bellName
                    }
                    views.setTextViewText(R.id.widget_title, labelText)
                } else {
                    views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                    views.setTextViewText(R.id.widget_title, "Bitti")
                }
            } else {
                views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                val isMultiline = WidgetStore.isMultilineEnabled(context)
                val labelText = if (isMultiline) {
                    views.setInt(R.id.widget_title, "setMaxLines", 2)
                    bellName.replace(" • ", "\n")
                } else {
                    views.setInt(R.id.widget_title, "setMaxLines", 1)
                    bellName
                }
                views.setTextViewText(R.id.widget_title, labelText)
            }

            val (startMinutes, endMinutes) = WidgetStore.getCurrentEventTimes(context)
            val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
            val isProgressBarVisible = WidgetStore.isProgressBarEnabled(context)

            if (isProgressBarVisible && startMinutes != -1 && endMinutes != -1 && nowMinutes >= startMinutes && nowMinutes <= endMinutes) {
                 val totalDuration = endMinutes - startMinutes
                 val elapsed = nowMinutes - startMinutes
                 if (totalDuration > 0) {
                      val progress = ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt()
                      views.setProgressBar(R.id.widget_progress_bar, 100, progress, false)
                      views.setViewVisibility(R.id.progress_container, View.VISIBLE)
                      views.setTextViewText(R.id.widget_percentage, "${progress}%")
                 }
            } else {
                views.setViewVisibility(R.id.progress_container, View.GONE)
            }
            
            val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, ModernCountdownWidget::class.java))
            for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
