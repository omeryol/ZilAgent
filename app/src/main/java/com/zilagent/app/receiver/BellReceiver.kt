package com.zilagent.app.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zilagent.app.R
import com.zilagent.app.widget.WidgetStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalTime

class BellReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bellName = intent.getStringExtra("BELL_NAME") ?: "Ders Zili"
        val isWidgetUpdate = intent.getBooleanExtra("IS_WIDGET_UPDATE", false)
        val isMinuteTick = intent.getBooleanExtra("IS_MINUTE_TICK", false)
        val dndAction = intent.getIntExtra("DND_ACTION", -1)
        val isCustomModeFinish = intent.getBooleanExtra("IS_CUSTOM_MODE_FINISH", false)

        if (isMinuteTick) {
            val manager = com.zilagent.app.manager.BellManager(context)
            manager.refreshWidgetState()
            manager.scheduleMinuteTick()
        }

        // Handle DND
        if (dndAction != -1 && WidgetStore.isAutoSilentMode(context)) {
            handleDnd(context, dndAction == 1)
        }

        // Play Sound if enabled
        if (WidgetStore.isSoundEnabled(context) && !isWidgetUpdate) {
            playSound(context)
        }

        if (isCustomModeFinish) {
            showNotification(context, "Süre Doldu!", "$bellName tamamlandı.")
            WidgetStore.setCustomCountdown(context, false, "", -1) // Auto-disable
            updateAllWidgets(context)
            return
        }

        if (isWidgetUpdate) {
            val goAsync = goAsync()
            GlobalScope.launch {
                try {
                    val db = com.zilagent.app.data.AppDatabase.getDatabase(context)
                    val profile = db.bellDao().getActiveProfileSync()
                    if (profile != null) {
                        val manager = com.zilagent.app.manager.BellManager(context)
                        val isHoliday = manager.isHolidayToday()
                        val (customEnabled, customTitle, customTime) = WidgetStore.getCustomCountdown(context)
                        val now = LocalTime.now()
                        val nowMinutes = now.hour * 60 + now.minute

                         if (customEnabled && customTime > nowMinutes) {
                            val displayTitle = if (customTitle.isNotEmpty()) customTitle else "Özel Sayaç"
                            val title = "🎯 $displayTitle • Bitiş: ${com.zilagent.app.util.TimeUtils.minutesToTime(customTime)}"
                            WidgetStore.setCurrentEventTimes(context, -1, customTime)
                            WidgetStore.updateNextBell(context, title, customTime)
                        } else if (isHoliday) {
                            WidgetStore.setCurrentEventTimes(context, -1, -1)
                            val quote = com.zilagent.app.util.QuoteConstants.getRandomQuoteFromDb(db.quoteDao())
                            WidgetStore.updateNextBell(context, quote, -1)
                        } else {
                            val schedules = db.bellDao().getSchedulesForProfileSync(profile.id)
                            val nextEvent = schedules.firstOrNull { it.endTime > nowMinutes }
                            if (nextEvent != null) {
                                val isOngoing = nowMinutes >= nextEvent.startTime
                                
                            val separator = if (WidgetStore.isMultilineEnabled(context)) "\n" else " • "
                            
                            val title = when {
                                isOngoing && !nextEvent.isBreak -> "⏳ ${nextEvent.name}${separator}Bitiş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.endTime)}"
                                isOngoing && nextEvent.isBreak -> "☕ ${nextEvent.name}${separator}Giriş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.endTime)}"
                                !isOngoing -> "🔔 ${nextEvent.name}${separator}Giriş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.startTime)}"
                                else -> nextEvent.name
                            }

                            val targetTime = if (isOngoing) nextEvent.endTime else nextEvent.startTime
                            
                            if (isOngoing) {
                                WidgetStore.setCurrentEventTimes(context, nextEvent.startTime, nextEvent.endTime)
                            } else {
                                // Progress during break: from previous event end to next event start
                                val previousEvent = schedules.lastOrNull { it.endTime <= nowMinutes }
                                // If first lesson of the day, use 0 or a reasonable start if you want. 
                                // Let's use previous event end or 0 if none.
                                val startTime = previousEvent?.endTime ?: 0
                                WidgetStore.setCurrentEventTimes(context, startTime, nextEvent.startTime)
                            }
                            WidgetStore.updateNextBell(context, title, targetTime)
                            } else {
                                WidgetStore.setCurrentEventTimes(context, -1, -1)
                                // Enhanced End of Day Message
                                val hour = now.hour
                                val baseMsg = when {
                                    hour >= 21 || hour < 5 -> "🌙 İyi Geceler"
                                     hour >= 17 -> "🌆 İyi Akşamlar"
                                    else -> "🔋 Dinlenme Vakti"
                                }
                                val quote = com.zilagent.app.util.QuoteConstants.getRandomQuoteFromDb(db.quoteDao())
                                WidgetStore.updateNextBell(context, "$baseMsg • $quote", -1)
                            }
                        }
                    }
                    updateAllWidgets(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    goAsync.finish()
                }
            }
        } else {
            val message = "Zilin çalmasına 1 dakika kaldı!"
            showNotification(context, bellName, message)
        }
    }

    private fun playSound(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateAllWidgets(context: Context) {
        com.zilagent.app.widget.CountdownWidget.updateAllWidgets(context)
        com.zilagent.app.widget.HorizontalCountdownWidget.updateAll(context)
        com.zilagent.app.widget.ModernCountdownWidget.updateAll(context)
        com.zilagent.app.widget.PanoramicCountdownWidget.updateAll(context)
        com.zilagent.app.widget.CircleCountdownWidget.updateAll(context)
    }

    private fun handleDnd(context: Context, enable: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val filter = if (enable) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL
            notificationManager.setInterruptionFilter(filter)
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        if (!WidgetStore.isNotificationsEnabled(context)) return
        
        val channelId = "zil_agent_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Zil Bildirimleri"
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) 
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }
}
