package com.greenicephoenix.traceledger

import android.app.Application
import com.greenicephoenix.traceledger.core.currency.NumberFormatManager
import com.greenicephoenix.traceledger.core.di.AppContainer
import com.greenicephoenix.traceledger.core.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TraceLedgerApp : Application() {

    // Application-scoped coroutine scope — survives configuration changes,
    // cancelled only when the process is killed.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)

        NumberFormatManager.init(this)

        // Create all notification channels (daily reminder + auto backup).
        NotificationHelper.createChannel(this)

        // Re-register the periodic backup WorkManager job on every app start.
        // WorkManager can lose scheduled jobs after APK updates on some devices.
        // This is a no-op if backup is disabled or no folder has been chosen.
        appScope.launch {
            container.scheduleBackupIfEnabled()
        }
    }
}