package com.greenicephoenix.traceledger.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "traceledger_settings"

val Context.settingsDataStore by preferencesDataStore(
    name = DATASTORE_NAME
)

object SettingsKeys {
    val CURRENCY_CODE = stringPreferencesKey("currency_code")
}

class SettingsDataStore(
    private val context: Context
) {

    val currencyCode: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.CURRENCY_CODE]
        }

    suspend fun setCurrency(code: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.CURRENCY_CODE] = code
        }
    }
}