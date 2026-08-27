package com.farmsos.domain.repository

import com.farmsos.core.error.AppError
import com.farmsos.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUser(id: String): Result<User>
    suspend fun getUserByEmail(email: String): Result<User>
    suspend fun insertUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(id: String): Result<Unit>
    fun getUsers(): Flow<List<User>>
}
