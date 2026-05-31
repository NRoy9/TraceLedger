package com.greenicephoenix.traceledger.core.backup

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.greenicephoenix.traceledger.core.database.TraceLedgerDatabase
import com.greenicephoenix.traceledger.core.datastore.SettingsDataStore
import com.greenicephoenix.traceledger.core.export.ExportService
import com.greenicephoenix.traceledger.core.notifications.NotificationHelper
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker that performs the periodic JSON auto-backup.
 *
 * Flow:
 * 1. Read the persisted folder URI from DataStore
 * 2. If no folder URI is set, abort gracefully (user hasn't set up backup yet)
 * 3. Call ExportService.exportJsonToFolder() to write a dated JSON file
 * 4. Post a "Backup saved" notification with the file name
 *
 * The worker is a CoroutineWorker so all DB and IO operations run on a
 * dispatcher managed by WorkManager — no manual thread management needed.
 *
 * Retry behaviour: if the worker fails (e.g. folder URI revoked),
 * WorkManager will retry with exponential backoff up to the periodic interval.
 */
class AutoBackupWorker(
    appContext: Context,
    params:     WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val settingsStore = SettingsDataStore(applicationContext)

            // Read the persisted folder URI
            val folderUriString = settingsStore.autoBackupFolderUri.first()
                ?: return Result.success()   // No folder chosen yet — skip silently

            val folderUri = Uri.parse(folderUriString)

            // Verify we still have permission to write to this URI
            val hasPermission = applicationContext.contentResolver.persistedUriPermissions
                .any { it.uri == folderUri && it.isWritePermission }

            if (!hasPermission) {
                // Permission was revoked (user cleared storage, uninstalled file manager, etc.)
                // Disable auto backup and return success so WorkManager doesn't keep retrying.
                settingsStore.setAutoBackupEnabled(false)
                settingsStore.setAutoBackupFolderUri(null)
                return Result.success()
            }

            val database      = TraceLedgerDatabase.getInstance(applicationContext)
            val exportService = ExportService(
                database        = database,
                contentResolver = applicationContext.contentResolver,
                settingsStore   = settingsStore   // ← add this line
            )

            val result = exportService.exportJsonToFolder(
                treeFolderUri = folderUri,
                context       = applicationContext
            )

            NotificationHelper.postBackupSuccess(
                context  = applicationContext,
                fileName = result.fileName
            )

            Result.success()

        } catch (e: Exception) {
            // Return failure — WorkManager will retry according to backoff policy
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "TraceLedger_AutoBackup"
    }
}