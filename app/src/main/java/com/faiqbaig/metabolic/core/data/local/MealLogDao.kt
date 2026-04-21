package com.faiqbaig.metabolic.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLogById(id: String)

    // Gets all meals for a specific user on a specific date (drives the Tracker UI)
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date ORDER BY timestamp ASC")
    fun getTodaysMeals(userId: String, date: String): Flow<List<MealLogEntity>>

    // Overloaded conceptually: exact same query, but useful if you want to reuse it by name
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date ORDER BY timestamp ASC")
    fun getMealsByDate(userId: String, date: String): Flow<List<MealLogEntity>>

    // For future use: Gets meals within a 7-day window to build charts/graphs
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getWeeklySummary(userId: String, startDate: String, endDate: String): Flow<List<MealLogEntity>>
}