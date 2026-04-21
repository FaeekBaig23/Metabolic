package com.faiqbaig.metabolic.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserProfileEntity::class, MealLogEntity::class], // Added MealLogEntity
    version = 3, // Bumped from 2 to 3
    exportSchema = false
)
abstract class MetabolicDatabase : RoomDatabase() {

    abstract val userProfileDao: UserProfileDao
    abstract val mealLogDao: MealLogDao // Added the new DAO

    companion object {
        // The migration script from v2 to v3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new meal_logs table exactly as defined in MealLogEntity
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `meal_logs` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `foodName` TEXT NOT NULL,
                        `calories` INTEGER NOT NULL,
                        `protein` INTEGER NOT NULL,
                        `carbs` INTEGER NOT NULL,
                        `fat` INTEGER NOT NULL,
                        `servingQty` REAL NOT NULL,
                        `servingUnit` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}