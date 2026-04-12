package com.faiqbaig.metabolic.di

import android.content.Context
import androidx.room.Room
import com.faiqbaig.metabolic.core.data.local.MetabolicDatabase
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
            .fallbackToDestructiveMigration()
            .build()
}
