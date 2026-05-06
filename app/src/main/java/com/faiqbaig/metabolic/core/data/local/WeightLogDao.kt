package com.faiqbaig.metabolic.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightLogEntity)

    @Delete
    suspend fun delete(entry: WeightLogEntity)

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllLogs(userId: String): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLog(userId: String): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getLogsInRange(userId: String, start: String, end: String): Flow<List<WeightLogEntity>>
}