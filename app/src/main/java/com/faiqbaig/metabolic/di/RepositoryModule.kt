package com.faiqbaig.metabolic.di

// Repository bindings will be added here as we build
// each feature module. Keeping this as a clean
// placeholder so the DI graph is ready to expand.

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule