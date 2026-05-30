package com.greenicephoenix.traceledger.core.backup

import java.util.concurrent.TimeUnit

/**
 * Frequency options for the periodic auto-backup.
 * [intervalHours] is passed directly to WorkManager's PeriodicWorkRequest.
 * WorkManager minimum interval is 15 minutes — all our values are well above that.
 */
enum class BackupFrequency(
    val label:         String,
    val intervalHours: Long
) {
    DAILY     ("Daily",      24L),
    WEEKLY    ("Weekly",     24L * 7),         // default
    BI_WEEKLY ("Bi-weekly",  24L * 14),
    MONTHLY   ("Monthly",    24L * 30),
    QUARTERLY ("Quarterly",  24L * 90);

    companion object {
        val DEFAULT = WEEKLY

        fun fromName(name: String?): BackupFrequency =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}