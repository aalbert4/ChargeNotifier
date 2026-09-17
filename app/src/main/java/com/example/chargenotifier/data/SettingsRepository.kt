package com.example.chargenotifier.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val CHARGING_NOTIFICATIONS_ENABLED = booleanPreferencesKey("charging_notifications_enabled")
        val ON_BODY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("on_body_notifications_enabled")
        val DISMISS_DELAY_SECONDS = intPreferencesKey("dismiss_delay_seconds")
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
        val LOW_BATTERY_NOTIFIED = booleanPreferencesKey("low_battery_notified")
    }

    val chargingNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CHARGING_NOTIFICATIONS_ENABLED] ?: true
    }

    val onBodyNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ON_BODY_NOTIFICATIONS_ENABLED] ?: true
    }

    val dismissDelaySeconds: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DISMISS_DELAY_SECONDS] ?: 10
    }

    val lowBatteryThreshold: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOW_BATTERY_THRESHOLD] ?: 20
    }

    val lowBatteryNotified: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOW_BATTERY_NOTIFIED] ?: false
    }

    suspend fun setChargingNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHARGING_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setOnBodyNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ON_BODY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDismissDelaySeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISMISS_DELAY_SECONDS] = seconds
        }
    }

    suspend fun setLowBatteryThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOW_BATTERY_THRESHOLD] = threshold
            // Reset notified flag if threshold changes? 
            // Actually better to just set it to false if new threshold is higher than current level.
            // But we'll handle reset logic in the service (e.g. when charging).
        }
    }

    suspend fun setLowBatteryNotified(notified: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOW_BATTERY_NOTIFIED] = notified
        }
    }
}
