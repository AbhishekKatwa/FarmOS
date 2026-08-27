package com.farmsos.domain.repository

import com.farmsos.domain.model.Farm
import kotlinx.coroutines.flow.Flow

interface FarmRepository {
    fun getAllFarms(): Flow<List<Farm>>
    suspend fun refreshFarms(): Result<List<Farm>>
    suspend fun getFarm(id: String): Result<Farm>
    suspend fun getFarmsByOwner(ownerId: String): Result<List<Farm>>
    suspend fun insertFarm(farm: Farm): Result<Unit>
    suspend fun createFarm(name: String, location: String, ownerId: String): Result<Farm>
    suspend fun updateFarm(farm: Farm): Result<Unit>
    suspend fun archiveFarm(id: String): Result<Unit>
    suspend fun deleteFarm(id: String): Result<Unit>
}
