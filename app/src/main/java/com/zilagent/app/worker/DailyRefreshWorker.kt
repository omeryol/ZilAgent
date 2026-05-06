package com.zilagent.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zilagent.app.data.AppDatabase
import com.zilagent.app.manager.BellManager
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * WorkManager task that runs every 24 hours to re-schedule daily alarms.
 * This ensures alarms are refreshed even if the device restarts or the
 * alarm chain breaks at midnight.
 */
class DailyRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val db = AppDatabase.getDatabase(context)
            val bellDao = db.bellDao()
            val bellManager = BellManager(context)

            val activeProfile = bellDao.getAllProfilesSync().firstOrNull { it.isActive }
                ?: bellDao.getAllProfilesSync().firstOrNull()

            if (activeProfile != null) {
                val today = LocalDate.now().dayOfWeek.value
                val todaySchedules = bellDao.getAllSchedulesForProfileSync(activeProfile.id)
                    .filter { it.dayOfWeek == today || it.dayOfWeek == 0 }
                    .sortedBy { it.orderIndex }

                bellManager.scheduleDailyAlarms(todaySchedules)
                Log.d(TAG, "Daily refresh completed for profile: ${activeProfile.name}, ${todaySchedules.size} schedules")
            } else {
                Log.d(TAG, "No active profile found, skipping daily refresh")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Daily refresh failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DailyRefreshWorker"
        private const val WORK_NAME = "daily_alarm_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyRefreshWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
