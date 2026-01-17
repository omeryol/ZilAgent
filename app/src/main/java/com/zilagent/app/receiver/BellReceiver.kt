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
                            WidgetStore.updateNextBell(context, "🌴 Tatil • $quote", -1)
                        } else {
                            val today = java.time.LocalDate.now().dayOfWeek.value
                            val schedules = db.bellDao().getSchedulesForProfileSync(profile.id, today)
                            
                            val nextEvent = schedules.firstOrNull { it.endTime > nowMinutes }
                            
                            if (nextEvent != null) {
                                val isOngoing = nowMinutes >= nextEvent.startTime
                                
                                val separator = " • "
                                
                                val title = when {
                                    isOngoing && !nextEvent.isBreak -> "⏳ ${nextEvent.name}${separator}Bitiş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.endTime)}"
                                    isOngoing && nextEvent.isBreak -> "☕ ${nextEvent.name}${separator}Giriş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.endTime)}"
                                    !isOngoing -> "🔔 ${nextEvent.name}${separator}Giriş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.startTime)}"
                                    else -> nextEvent.name
                                }
    
                                val targetTime = if (isOngoing) nextEvent.endTime else nextEvent.startTime
                                
                                var syllabusInfo: String? = null
                                var classColor: String? = null
                                
                                if (!nextEvent.isBreak) {
                                    // Count previous lessons to find order
                                    val lessonOrder = schedules.filter { !it.isBreak }.indexOf(nextEvent) + 1
                                    val fullInfo = db.syllabusDao().getFullSyllabusEntrySync(profile.id, today, lessonOrder)
                                    if (fullInfo != null) {
                                        val className = fullInfo.className
                                        val subName = fullInfo.subjectName
                                        if (className != null && subName != null) {
                                            syllabusInfo = "$className - $subName"
                                            classColor = fullInfo.classColor
                                        } else if (className != null) {
                                            syllabusInfo = className
                                            classColor = fullInfo.classColor
                                        } else if (subName != null) {
                                            syllabusInfo = subName
                                        }
                                    }
                                }

                                val eventStartTime = if (isOngoing) {
                                    nextEvent.startTime
                                } else {
                                    val previousEvent = schedules.lastOrNull { it.endTime <= nowMinutes }
                                    previousEvent?.endTime ?: 0
                                }
                                
                                WidgetStore.setCurrentEventTimes(context, eventStartTime, targetTime)
                                
                                // Construct the final title with syllabus info if available
                                val displayTitle = if (syllabusInfo != null) {
                                    val prefix = if (isOngoing) "⏳ " else "🔔 "
                                    val suffix = if (isOngoing) "Bitiş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.endTime)}" 
                                                 else "Giriş: ${com.zilagent.app.util.TimeUtils.minutesToTime(nextEvent.startTime)}"
                                    "$prefix$syllabusInfo • $suffix"
                                } else {
                                    title
                                }

                                WidgetStore.updateNextBell(context, displayTitle, targetTime, syllabusInfo, classColor, eventStartTime)
                            } else {
                                WidgetStore.setCurrentEventTimes(context, -1, -1)
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
        com.zilagent.app.widget.PanoramicCountdownWidget.updateAll(context)
        com.zilagent.app.widget.SyllabusWidget.updateAll(context)
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
