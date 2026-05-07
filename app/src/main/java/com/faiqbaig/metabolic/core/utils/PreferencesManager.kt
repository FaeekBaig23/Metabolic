package com.faiqbaig.metabolic.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "metabolic_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        // ── NEW: DataStore Keys for Hydration ──
        val WATER_CONSUMED = intPreferencesKey("water_consumed")
        val LAST_WATER_DATE = stringPreferencesKey("last_water_date")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    // ── NEW: Flow to observe daily water ──
    val waterConsumedFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[LAST_WATER_DATE] ?: ""
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // If the saved date is today, return the amount. Otherwise, return 0.
        if (lastDate == today) {
            preferences[WATER_CONSUMED] ?: 0
        } else {
            0
        }
    }

    // ── NEW: Suspend function to save water ──
    suspend fun addWater(amount: Int) {
        context.dataStore.edit { preferences ->
            val lastDate = preferences[LAST_WATER_DATE] ?: ""
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val currentAmount = if (lastDate == today) {
                preferences[WATER_CONSUMED] ?: 0
            } else {
                0 // Reset if a new day has started
            }

            preferences[WATER_CONSUMED] = currentAmount + amount
            preferences[LAST_WATER_DATE] = today
        }
    }
}