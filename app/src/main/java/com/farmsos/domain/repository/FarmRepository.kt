package com.farmsos.domain.repository

import com.farmsos.core.error.AppError
import com.farmsos.domain.model.Farm
import kotlinx.coroutines.flow.Flow

interface FarmRepository {
    suspend fun getFarm(id: String): Result<Farm>
    suspend fun getFarmsByOwner(ownerId: String): Result<List<Farm>>
    suspend fun insertFarm(farm: Farm): Result<Unit>
    suspend fun updateFarm(farm: Farm): Result<Unit>
    suspend fun deleteFarm(id: String): Result<Unit>
    fun getAllFarms(): Flow<List<Farm>>
}
