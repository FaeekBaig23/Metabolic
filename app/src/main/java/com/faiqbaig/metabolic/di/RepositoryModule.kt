package com.faiqbaig.metabolic.di

import android.content.Context
import com.faiqbaig.metabolic.core.data.local.UserProfileDao
import com.faiqbaig.metabolic.core.data.local.WeightLogDao
import com.faiqbaig.metabolic.core.data.repository.MealLogRepositoryImpl
import com.faiqbaig.metabolic.core.data.repository.WeightLogRepositoryImpl
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import com.faiqbaig.metabolic.core.data.repository.WeightLogRepository
import com.faiqbaig.metabolic.core.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        dao      : UserProfileDao,
        auth     : FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserProfileRepository = UserProfileRepository(dao, auth, firestore)

    // ── CHANGED: Swapped @Binds for @Provides so it works perfectly inside an 'object' ──
    @Provides
    @Singleton
    fun provideMealLogRepository(
        mealLogRepositoryImpl: MealLogRepositoryImpl
    ): MealLogRepository = mealLogRepositoryImpl

    @Provides
    @Singleton
    fun provideWeightLogRepository(
        weightLogDao: WeightLogDao
    ): WeightLogRepository {
        return WeightLogRepositoryImpl(weightLogDao)
    }
}