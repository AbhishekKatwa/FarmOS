package com.farmsos.domain.usecase.farm

import com.farmsos.domain.model.Shed
import com.farmsos.domain.repository.FarmRepository
import com.farmsos.domain.repository.ShedRepository
import javax.inject.Inject

class ListShedsUseCase @Inject constructor(
    private val shedRepository: ShedRepository
) {
    suspend operator fun invoke(farmId: String): Result<List<Shed>> = shedRepository.listByFarm(farmId)
}

class CreateShedUseCase @Inject constructor(
    private val farmRepository: FarmRepository,
    private val shedRepository: ShedRepository
) {
    suspend operator fun invoke(farmId: String, name: String, capacity: Int?, notes: String): Result<Shed> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Shed name is required"))
        }
        if (capacity != null && capacity <= 0) {
            return Result.failure(IllegalArgumentException("Capacity must be greater than 0"))
        }
        farmRepository.getFarm(farmId).getOrElse {
            return Result.failure(IllegalArgumentException("Shed must belong to a valid farm"))
        }
        return shedRepository.createShed(farmId, trimmed, capacity, notes.trim())
    }
}

class ArchiveShedUseCase @Inject constructor(
    private val shedRepository: ShedRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = shedRepository.archiveShed(id)
}
