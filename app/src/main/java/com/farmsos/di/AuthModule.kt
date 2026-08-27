package com.farmsos.di

import com.farmsos.data.repository.AuthRepositoryImpl
import com.farmsos.data.repository.ProfileRepositoryImpl
import com.farmsos.domain.repository.AuthRepository
import com.farmsos.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
