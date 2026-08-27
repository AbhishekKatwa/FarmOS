package com.farmsos.di

import android.content.Context
import androidx.room.Room
import com.farmsos.core.logging.AppLogger
import com.farmsos.core.network.ApiClient
import com.farmsos.core.network.NetworkManager
import com.farmsos.domain.repository.UserRepository
import com.farmsos.data.local.FarmDatabase
import com.farmsos.data.local.UserDatabase
import com.farmsos.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideLogger(): AppLogger {
        return AppLogger()
    }

    @Provides
    @Singleton
    fun provideNetworkManager(
        @ApplicationContext context: Context
    ): NetworkManager {
        return NetworkManager(context)
    }

    @Provides
    @Singleton
    fun provideApiClient(networkManager: NetworkManager): ApiClient {
        return ApiClient(networkManager)
    }

    @Provides
    @Singleton
    fun provideFarmDatabase(@ApplicationContext context: Context): FarmDatabase {
        return Room.databaseBuilder(
            context,
            FarmDatabase::class.java,
            "farm_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase {
        return Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "user_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiClient: ApiClient,
        userDatabase: UserDatabase,
        logger: AppLogger
    ): UserRepository {
        return UserRepositoryImpl(apiClient, userDatabase, logger)
    }
}
