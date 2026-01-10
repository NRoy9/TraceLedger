package com.greenicephoenix.traceledger

import android.app.Application
import com.greenicephoenix.traceledger.core.di.AppContainer

class TraceLedgerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}