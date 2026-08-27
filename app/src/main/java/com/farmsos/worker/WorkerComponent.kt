package com.farmsos.worker

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Worker module for Hilt dependency injection
 * Provides WorkManager related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
interface WorkerComponent