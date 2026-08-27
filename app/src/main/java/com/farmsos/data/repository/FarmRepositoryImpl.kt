package com.farmsos.data.repository

import com.farmsos.core.error.AppError
import com.farmsos.core.logging.AppLogger
import com.farmsos.core.network.ApiClient
import com.farmsos.data.local.FarmDao
import com.farmsos.data.local.FarmDatabase
import com.farmsos.domain.model.Farm
import com.farmsos.domain.repository.FarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FarmRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
    private val farmDatabase: FarmDatabase,
    private val logger: AppLogger
) : FarmRepository {

    private val farmDao: FarmDao = farmDatabase.farmDao()

    override suspend fun getFarm(id: String): Result<Farm> {
        return try {
            val farm = farmDao.getFarmById(id)
            if (farm != null) {
                Result.success(farm)
            } else {
                Result.failure(Exception("Farm not found"))
            }
        } catch (e: Exception) {
            logger.e("Error getting farm: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getFarmsByOwner(ownerId: String): Result<List<Farm>> {
        return try {
            val farms = farmDao.getFarmsByOwner(ownerId)
            Result.success(farms)
        } catch (e: Exception) {
            logger.e("Error getting farms by owner: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun insertFarm(farm: Farm): Result<Unit> {
        return try {
            farmDao.insertFarm(farm)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("Error inserting farm: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateFarm(farm: Farm): Result<Unit> {
        return try {
            farmDao.updateFarm(farm)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("Error updating farm: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteFarm(id: String): Result<Unit> {
        return try {
            val farm = farmDao.getFarmById(id)
            if (farm != null) {
                farmDao.deleteFarm(farm)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Farm not found"))
            }
        } catch (e: Exception) {
            logger.e("Error deleting farm: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getAllFarms(): Flow<List<Farm>> {
        return farmDao.getAllFarms()
    }
}
