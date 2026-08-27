package com.farmsos.testing

import com.farmsos.domain.model.Farm
import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.model.Shed
import com.farmsos.domain.repository.FarmRepository
import com.farmsos.domain.repository.FlockRepository
import com.farmsos.domain.repository.ShedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFarmRepository : FarmRepository {
    private val farms = MutableStateFlow<List<Farm>>(emptyList())

    fun seed(farm: Farm) {
        farms.value = farms.value + farm
    }

    override fun getAllFarms(): Flow<List<Farm>> = farms
    override suspend fun refreshFarms(): Result<List<Farm>> = Result.success(farms.value)
    override suspend fun getFarm(id: String): Result<Farm> =
        farms.value.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(IllegalArgumentException("Farm not found"))
    override suspend fun getFarmsByOwner(ownerId: String): Result<List<Farm>> =
        Result.success(farms.value.filter { it.ownerId == ownerId })
    override suspend fun insertFarm(farm: Farm): Result<Unit> {
        farms.value = farms.value + farm
        return Result.success(Unit)
    }
    override suspend fun createFarm(name: String, location: String, ownerId: String): Result<Farm> {
        val farm = Farm(id = "farm-${farms.value.size + 1}", name = name, location = location, ownerId = ownerId, createdAt = 1, updatedAt = 1)
        farms.value = farms.value + farm
        return Result.success(farm)
    }
    override suspend fun updateFarm(farm: Farm): Result<Unit> {
        farms.value = farms.value.map { if (it.id == farm.id) farm else it }
        return Result.success(Unit)
    }
    override suspend fun archiveFarm(id: String): Result<Unit> {
        farms.value = farms.value.map { if (it.id == id) it.copy(isActive = false) else it }
        return Result.success(Unit)
    }
    override suspend fun deleteFarm(id: String): Result<Unit> = archiveFarm(id)
}

class FakeShedRepository : ShedRepository {
    private val sheds = mutableListOf<Shed>()

    fun seed(shed: Shed) {
        sheds += shed
    }

    override suspend fun listByFarm(farmId: String, includeArchived: Boolean): Result<List<Shed>> =
        Result.success(sheds.filter { it.farmId == farmId && (includeArchived || it.isActive) })

    override suspend fun getShed(id: String): Result<Shed> =
        sheds.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(IllegalArgumentException("Shed not found"))

    override suspend fun createShed(farmId: String, name: String, capacity: Int?, notes: String): Result<Shed> {
        val shed = Shed(id = "shed-${sheds.size + 1}", farmId = farmId, name = name, capacity = capacity, notes = notes, isActive = true, createdAt = 1, updatedAt = 1)
        sheds += shed
        return Result.success(shed)
    }

    override suspend fun updateShed(shed: Shed): Result<Shed> {
        val index = sheds.indexOfFirst { it.id == shed.id }
        if (index < 0) return Result.failure(IllegalArgumentException("Shed not found"))
        sheds[index] = shed
        return Result.success(shed)
    }

    override suspend fun archiveShed(id: String): Result<Unit> {
        val index = sheds.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(IllegalArgumentException("Shed not found"))
        sheds[index] = sheds[index].copy(isActive = false)
        return Result.success(Unit)
    }
}

class FakeFlockRepository : FlockRepository {
    private val flocks = mutableListOf<Flock>()

    override suspend fun listByFarm(farmId: String): Result<List<Flock>> =
        Result.success(flocks.filter { it.farmId == farmId })

    override suspend fun listByShed(shedId: String): Result<List<Flock>> =
        Result.success(flocks.filter { it.shedId == shedId })

    override suspend fun getFlock(id: String): Result<Flock> =
        flocks.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(IllegalArgumentException("Flock not found"))

    override suspend fun createFlock(flock: Flock): Result<Flock> {
        val stored = flock.copy(id = "flock-${flocks.size + 1}")
        flocks += stored
        return Result.success(stored)
    }

    override suspend fun updateFlock(flock: Flock): Result<Flock> {
        val index = flocks.indexOfFirst { it.id == flock.id }
        if (index < 0) return Result.failure(IllegalArgumentException("Flock not found"))
        flocks[index] = flock
        return Result.success(flock)
    }

    override suspend fun archiveFlock(id: String): Result<Unit> {
        val index = flocks.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(IllegalArgumentException("Flock not found"))
        flocks[index] = flocks[index].copy(status = FlockStatus.CLOSED)
        return Result.success(Unit)
    }
}
