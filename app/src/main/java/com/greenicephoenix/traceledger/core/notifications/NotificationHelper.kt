package com.greenicephoenix.traceledger.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.greenicephoenix.traceledger.MainActivity
import com.greenicephoenix.traceledger.R
import com.greenicephoenix.traceledger.core.ui.theme.SovereignViolet

/**
 * Manages notification channels and posting of the daily reminder notification.
 *
 * Android 8 (API 26) and above require notifications to be assigned to a channel.
 * The channel is created here. Creating it is idempotent — calling it multiple times
 * is safe and has no effect after the first call.
 *
 * Why a single object? Because both the receiver (DailyReminderReceiver) and the
 * app startup (TraceLedgerApp) need to call createChannel(), and we want one place
 * for all notification-related logic.
 */
object NotificationHelper {

    const val CHANNEL_ID      = "daily_reminder"
    const val CHANNEL_BACKUP  = "auto_backup"
    const val NOTIFICATION_ID = 1001
    const val NOTIFICATION_BACKUP_ID = 1002

    /**
     * Creates all notification channels used by the app.
     * Must be called before any notification can be posted on Android 8+.
     * Safe to call multiple times — Android ignores duplicate channel creation.
     */
    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Daily reminder channel
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Daily Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Remind you to log your daily transactions"
            }
        )

        // Auto backup channel — LOW importance: no sound, just drawer entry
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BACKUP,
                "Auto Backup",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies when a scheduled backup completes"
            }
        )
    }

    /**
     * Posts the daily reminder notification.
     * Tapping it opens MainActivity (which then shows the app's last state).
     *
     * FLAG_IMMUTABLE is required on Android 12+ for PendingIntents.
     */
    fun postReminder(context: Context) {
        // Create a PendingIntent so tapping the notification opens the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)          // app icon in the notification shade
            .setColor(SovereignViolet.toArgb())
            .setContentTitle("TraceLedger")
            .setContentText("Don't forget to log today's transactions!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)                         // dismiss when tapped
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Posts a silent backup-complete notification.
     * [fileName] is the name of the file that was written (shown in the body).
     * Tapping opens the app.
     */
    fun postBackupSuccess(context: Context, fileName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BACKUP)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(SovereignViolet.toArgb())
            .setContentTitle("Backup saved")
            .setContentText(fileName)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_BACKUP_ID, notification)
    }
}