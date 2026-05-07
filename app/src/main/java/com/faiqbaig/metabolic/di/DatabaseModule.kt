package com.faiqbaig.metabolic.di

import android.content.Context
import androidx.room.Room
import com.faiqbaig.metabolic.core.data.local.MealLogDao
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
            // ── CHANGED: Added MIGRATION_3_4 to the array ──
            .addMigrations(
                MetabolicDatabase.MIGRATION_2_3,
                MetabolicDatabase.MIGRATION_3_4
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
    fun provideWeightLogDao(database: MetabolicDatabase): WeightLogDao {
        return database.weightLogDao
    }
}