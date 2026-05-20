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
        // ── DataStore Keys for Hydration ──
        val WATER_CONSUMED = intPreferencesKey("water_consumed")
        val LAST_WATER_DATE = stringPreferencesKey("last_water_date")

        // ── NEW: Step 10 App Settings Keys ──
        val DAILY_WATER_TARGET_ML = intPreferencesKey("daily_water_target_ml")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val HEIGHT_UNIT = stringPreferencesKey("height_unit")
        val MEAL_REMINDERS = booleanPreferencesKey("meal_reminders_enabled")
        val HYDRATION_REMINDERS = booleanPreferencesKey("hydration_reminders")
        val WEIGHT_REMINDERS = booleanPreferencesKey("weight_reminders")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    // ── Flow to observe daily water ──
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

    // ── Suspend function to save water ──
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

    // ── NEW: Step 10 App Settings Flows ──
    val dailyWaterTarget: Flow<Int> = context.dataStore.data.map { it[DAILY_WATER_TARGET_ML] ?: 2500 }
    val weightUnit: Flow<String> = context.dataStore.data.map { it[WEIGHT_UNIT] ?: "kg" }
    val heightUnit: Flow<String> = context.dataStore.data.map { it[HEIGHT_UNIT] ?: "cm" }
    val mealReminders: Flow<Boolean> = context.dataStore.data.map { it[MEAL_REMINDERS] ?: false }
    val hydrationReminders: Flow<Boolean> = context.dataStore.data.map { it[HYDRATION_REMINDERS] ?: false }
    val weightReminders: Flow<Boolean> = context.dataStore.data.map { it[WEIGHT_REMINDERS] ?: false }

    // ── NEW: Step 10 App Settings Update Functions ──
    suspend fun setDailyWaterTarget(targetMl: Int) {
        context.dataStore.edit { it[DAILY_WATER_TARGET_ML] = targetMl }
    }

    suspend fun setWeightUnit(unit: String) {
        context.dataStore.edit { it[WEIGHT_UNIT] = unit }
    }

    suspend fun setHeightUnit(unit: String) {
        context.dataStore.edit { it[HEIGHT_UNIT] = unit }
    }

    suspend fun setMealReminders(enabled: Boolean) {
        context.dataStore.edit { it[MEAL_REMINDERS] = enabled }
    }

    suspend fun setHydrationReminders(enabled: Boolean) {
        context.dataStore.edit { it[HYDRATION_REMINDERS] = enabled }
    }

    suspend fun setWeightReminders(enabled: Boolean) {
        context.dataStore.edit { it[WEIGHT_REMINDERS] = enabled }
    }
}