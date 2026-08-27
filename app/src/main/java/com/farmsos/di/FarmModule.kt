package com.farmsos.di

import com.farmsos.data.repository.FarmRepositoryImpl
import com.farmsos.data.repository.FlockRepositoryImpl
import com.farmsos.data.repository.ShedRepositoryImpl
import com.farmsos.domain.repository.FarmRepository
import com.farmsos.domain.repository.FlockRepository
import com.farmsos.domain.repository.ShedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FarmModule {

    @Binds
    @Singleton
    abstract fun bindFarmRepository(impl: FarmRepositoryImpl): FarmRepository

    @Binds
    @Singleton
    abstract fun bindShedRepository(impl: ShedRepositoryImpl): ShedRepository

    @Binds
    @Singleton
    abstract fun bindFlockRepository(impl: FlockRepositoryImpl): FlockRepository
}
