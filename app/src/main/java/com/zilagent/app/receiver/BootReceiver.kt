package com.zilagent.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.manager.BellManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val bellManager = BellManager(context)
            val db = AppDatabase.getDatabase(context)
            
            val goAsync = goAsync()
            GlobalScope.launch {
                try {
                    val profile = db.bellDao().getActiveProfileSync()
                    if (profile != null) {
                        val schedules = db.bellDao().getSchedulesForProfileSync(profile.id)
                        bellManager.scheduleDailyAlarms(schedules)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    goAsync.finish()
                }
            }
        }
    }
}
