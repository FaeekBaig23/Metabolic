package com.faiqbaig.metabolic.di

import android.content.Context
import androidx.room.Room
import com.faiqbaig.metabolic.core.data.local.MealLogDao // Make sure to add this import
import com.faiqbaig.metabolic.core.data.local.WeightLogDao
import com.faiqbaig.metabolic.core.data.local.MetabolicDatabase
import com.faiqbaig.metabolic.core.data.local.UserProfileDao
import com.faiqbaig.metabolic.core.utils.PreferencesManager
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
            // ── CHANGED: Replaced destructive migration with proper v2 -> v3 migration ──
            .addMigrations(MetabolicDatabase.MIGRATION_2_3)
            .build()

    @Provides
    @Singleton
    fun provideUserProfileDao(database: MetabolicDatabase): UserProfileDao =
        database.userProfileDao // Or database.userProfileDao, depending on how it's defined in your DB

    // ── NEW: Provide the MealLogDao for Dependency Injection ──
    @Provides
    @Singleton
    fun provideMealLogDao(database: MetabolicDatabase): MealLogDao =
        database.mealLogDao // Or database.mealLogDao() if you defined it as a function in MetabolicDatabase

    @Provides
    fun provideWeightLogDao(database: MetabolicDatabase): WeightLogDao {
        return database.weightLogDao
    }
}