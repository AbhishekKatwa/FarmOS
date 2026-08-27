package com.farmsos.data.repository

import com.farmsos.core.error.AppError
import com.farmsos.core.logging.AppLogger
import com.farmsos.core.network.ApiClient
import com.farmsos.data.local.UserDao
import com.farmsos.data.local.UserDatabase
import com.farmsos.domain.model.User
import com.farmsos.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
    private val userDatabase: UserDatabase,
    private val logger: AppLogger
) : UserRepository {

    private val userDao: UserDao = userDatabase.userDao()

    override suspend fun getUser(id: String): Result<User> {
        return try {
            val user = userDao.getUserById(id)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            logger.e("Error getting user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserByEmail(email: String): Result<User> {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            logger.e("Error getting user by email: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun insertUser(user: User): Result<Unit> {
        return try {
            userDao.insertUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("Error inserting user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("Error updating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            val user = userDao.getUserById(id)
            if (user != null) {
                userDao.deleteUser(user)
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            logger.e("Error deleting user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
}
