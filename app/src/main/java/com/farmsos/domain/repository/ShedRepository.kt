package com.farmsos.domain.repository

import com.farmsos.domain.model.Shed

interface ShedRepository {
    suspend fun listByFarm(farmId: String, includeArchived: Boolean = false): Result<List<Shed>>
    suspend fun getShed(id: String): Result<Shed>
    suspend fun createShed(farmId: String, name: String, capacity: Int?, notes: String): Result<Shed>
    suspend fun updateShed(shed: Shed): Result<Shed>
    suspend fun archiveShed(id: String): Result<Unit>
}
