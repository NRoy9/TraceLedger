package com.greenicephoenix.traceledger.core.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Manages scheduling of the periodic auto-backup WorkManager job.
 *
 * Why a unique work name?
 * WorkManager's [ExistingPeriodicWorkPolicy.UPDATE] replaces any existing
 * enqueued work with the same name. This means calling [schedule] with a new
 * frequency automatically cancels the old job — no manual cancel needed.
 *
 * Constraints:
 * - NETWORK_TYPE = NOT_REQUIRED — backup writes to local storage, no internet needed
 * - requiresBatteryNotLow = true — don't run when battery is critically low
 */
object AutoBackupScheduler {

    /**
     * Schedule (or reschedule) the periodic backup.
     * Safe to call on every app start — UPDATE policy is idempotent if nothing changed.
     */
    fun schedule(context: Context, frequency: BackupFrequency) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            repeatInterval     = frequency.intervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AutoBackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,  // replaces existing job with new interval
            request
        )
    }

    /**
     * Cancel the periodic backup job.
     * Called when the user disables auto backup in Settings.
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(AutoBackupWorker.WORK_NAME)
    }
}