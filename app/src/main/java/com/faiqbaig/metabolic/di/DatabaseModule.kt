package com.faiqbaig.metabolic.di

import android.content.Context
import androidx.room.Room
import com.faiqbaig.metabolic.core.data.local.DietPlanDao // Added import
import com.faiqbaig.metabolic.core.data.local.MealLogDao
import com.faiqbaig.metabolic.core.data.local.WeightLogDao
import com.faiqbaig.metabolic.core.data.local.MetabolicDatabase
import com.faiqbaig.metabolic.core.data.local.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MetabolicDatabase =
        Room.databaseBuilder(
            context,
            MetabolicDatabase::class.java,
            "metabolic_db"
        )
            .addMigrations(
                MetabolicDatabase.MIGRATION_2_3, // Restored
                MetabolicDatabase.MIGRATION_3_4,
                MetabolicDatabase.MIGRATION_4_5  // Added for Step 9
            )
            .build()

    @Provides
    @Singleton
    fun provideUserProfileDao(database: MetabolicDatabase): UserProfileDao =
        database.userProfileDao

    @Provides
    @Singleton
    fun provideMealLogDao(database: MetabolicDatabase): MealLogDao =
        database.mealLogDao

    @Provides
    @Singleton // Added Singleton for consistency
    fun provideWeightLogDao(database: MetabolicDatabase): WeightLogDao {
        return database.weightLogDao
    }

    // --- Added for Step 9 ---
    @Provides
    @Singleton
    fun provideDietPlanDao(database: MetabolicDatabase): DietPlanDao =
        database.dietPlanDao
}