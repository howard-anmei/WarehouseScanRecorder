package com.anmei.warehouseputaway.work

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DailyExportScheduler {

    private const val TAG = "DailyExportScheduler"

    private const val WORK_NAME = "daily_scan_export"

    /**
     * Schedule the next automatic export at 22:00.
     *
     * Uses OneTimeWorkRequest so every execution
     * is explicitly scheduled for the next 22:00.
     */
    fun scheduleNext(context: Context) {

        val now = Calendar.getInstance()

        val nextRun = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            /*
             * If today's 22:00 has already passed,
             * schedule tomorrow at 22:00.
             *
             * If it is exactly 22:00, also schedule
             * the next day's 22:00 to avoid an immediate
             * duplicate execution.
             */
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay =
            nextRun.timeInMillis - now.timeInMillis

        Log.i(TAG, "==========================================")
        Log.i(TAG, "Scheduling daily scan export")
        Log.i(TAG, "Now       = ${now.time}")
        Log.i(TAG, "Next run  = ${nextRun.time}")
        Log.i(
            TAG,
            "Delay     = ${initialDelay / 1000}s"
        )
        Log.i(
            TAG,
            "Delay     = ${initialDelay / 60000}min"
        )
        Log.i(TAG, "Target    = 22:00")
        Log.i(TAG, "==========================================")

        val request =
            OneTimeWorkRequestBuilder<DailyExportWorker>()
                .setInitialDelay(
                    initialDelay,
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )

        Log.i(
            TAG,
            "Next export scheduled successfully"
        )
    }
}