package com.faiqbaig.metabolic.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        MealLogEntity::class,
        WeightLogEntity::class,
        DietPlanEntity::class,      // Added
        DietPlanMealEntity::class   // Added
    ],
    version = 5, // Bumped from 4 to 5
    exportSchema = false
)
abstract class MetabolicDatabase : RoomDatabase() {

    abstract val userProfileDao: UserProfileDao
    abstract val mealLogDao: MealLogDao
    abstract val weightLogDao: WeightLogDao
    abstract val dietPlanDao: DietPlanDao // Added the new DAO

    companion object {
        // The migration script from v2 to v3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
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

        // The migration script from v3 to v4
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new weight_logs table exactly as defined in WeightLogEntity
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weight_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `weightKg` REAL NOT NULL,
                        `bmi` REAL NOT NULL,
                        `date` TEXT NOT NULL,
                        `note` TEXT,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // The migration script from v4 to v5
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create diet_plans table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `diet_plans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `generatedAt` INTEGER NOT NULL, 
                        `weekStartDate` TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                // Create diet_plan_meals table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `diet_plan_meals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `planId` INTEGER NOT NULL, 
                        `dayIndex` INTEGER NOT NULL, 
                        `mealType` TEXT NOT NULL, 
                        `foodName` TEXT NOT NULL, 
                        `calories` INTEGER NOT NULL, 
                        `protein` REAL NOT NULL, 
                        `carbs` REAL NOT NULL, 
                        `fat` REAL NOT NULL, 
                        `estimatedWeightG` REAL NOT NULL, 
                        FOREIGN KEY(`planId`) REFERENCES `diet_plans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                    """.trimIndent()
                )

                // Create index for foreign key to prevent full table scans on cascade deletes
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_diet_plan_meals_planId` ON `diet_plan_meals` (`planId`)")
            }
        }
    }
}