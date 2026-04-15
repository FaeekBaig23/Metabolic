package com.faiqbaig.metabolic.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities     = [UserEntity::class, UserProfileEntity::class],
    version      = 2,
    exportSchema = true
)
abstract class MetabolicDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}