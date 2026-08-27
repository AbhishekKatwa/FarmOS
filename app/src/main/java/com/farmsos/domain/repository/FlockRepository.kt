package com.farmsos.domain.repository

import com.farmsos.domain.model.Flock

interface FlockRepository {
    suspend fun listByFarm(farmId: String): Result<List<Flock>>
    suspend fun listByShed(shedId: String): Result<List<Flock>>
    suspend fun getFlock(id: String): Result<Flock>
    suspend fun createFlock(flock: Flock): Result<Flock>
    suspend fun updateFlock(flock: Flock): Result<Flock>
    suspend fun archiveFlock(id: String): Result<Unit>
}
