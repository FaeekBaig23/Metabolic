package com.faiqbaig.metabolic.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getProfileOnce(userId: String): UserProfileEntity?

    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)
}