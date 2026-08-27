package com.farmsos.di

import com.farmsos.data.repository.FarmRepositoryImpl
import com.farmsos.data.repository.FlockRepositoryImpl
import com.farmsos.data.repository.ShedRepositoryImpl
import com.farmsos.data.repository.ProductionRepositoryImpl
import com.farmsos.data.repository.FeedRepositoryImpl
import com.farmsos.data.repository.HealthRepositoryImpl
import com.farmsos.data.repository.SalesRepositoryImpl
import com.farmsos.data.repository.FinanceRepositoryImpl
import com.farmsos.data.repository.DashboardRepositoryImpl
import com.farmsos.domain.repository.FarmRepository
import com.farmsos.domain.repository.FlockRepository
import com.farmsos.domain.repository.ShedRepository
import com.farmsos.domain.repository.ProductionRepository
import com.farmsos.domain.repository.FeedRepository
import com.farmsos.domain.repository.HealthRepository
import com.farmsos.domain.repository.SalesRepository
import com.farmsos.domain.repository.FinanceRepository
import com.farmsos.domain.repository.DashboardRepository
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

    @Binds
    @Singleton
    abstract fun bindProductionRepository(impl: ProductionRepositoryImpl): ProductionRepository
    @Binds @Singleton abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository
    @Binds @Singleton abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository
    @Binds @Singleton abstract fun bindSalesRepository(impl: SalesRepositoryImpl): SalesRepository
    @Binds @Singleton abstract fun bindFinanceRepository(impl: FinanceRepositoryImpl): FinanceRepository
    @Binds @Singleton abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
