package com.greenicephoenix.traceledger.core.currency

import android.content.Context
import com.greenicephoenix.traceledger.core.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for selected currency.
 * Backed by DataStore.
 */
object CurrencyManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currency = MutableStateFlow(Currency.INR)
    val currency: StateFlow<Currency> = _currency.asStateFlow()

    private var dataStore: SettingsDataStore? = null

    fun init(context: Context) {
        if (dataStore != null) return

        dataStore = SettingsDataStore(context)

        scope.launch {
            dataStore!!
                .currencyCode
                .collect { code ->
                    val resolved = Currency.values()
                        .firstOrNull { it.code == code }
                        ?: Currency.INR

                    _currency.value = resolved
                }
        }
    }

    fun setCurrency(currency: Currency) {
        _currency.value = currency

        scope.launch {
            dataStore?.setCurrency(currency.code)
        }
    }
}