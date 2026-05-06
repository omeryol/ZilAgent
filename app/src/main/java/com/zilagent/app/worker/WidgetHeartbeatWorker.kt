package com.zilagent.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zilagent.app.manager.BellManager
import java.util.concurrent.TimeUnit

/**
 * Periodic watchdog that keeps widget countdown updates alive even when minute alarms
 * are delayed by Doze/battery optimisation.
 */
class WidgetHeartbeatWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val bellManager = BellManager(applicationContext)
            bellManager.refreshWidgetState()
            bellManager.scheduleMinuteTick()
            Log.d(TAG, "Widget heartbeat refresh completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Widget heartbeat refresh failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "WidgetHeartbeatWorker"
        private const val WORK_NAME = "widget_heartbeat_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetHeartbeatWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
