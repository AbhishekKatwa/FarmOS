package com.farmsos.domain.usecase.farm

import com.farmsos.domain.model.Farm
import com.farmsos.domain.repository.FarmRepository
import javax.inject.Inject

class ListFarmsUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    operator fun invoke() = farmRepository.getAllFarms()

    suspend fun refresh(): Result<List<Farm>> = farmRepository.refreshFarms()
}

class CreateFarmUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(name: String, location: String, ownerId: String): Result<Farm> {
        val trimmedName = name.trim()
        val trimmedLocation = location.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Farm name is required"))
        }
        if (trimmedLocation.isBlank()) {
            return Result.failure(IllegalArgumentException("Location is required"))
        }
        if (ownerId.isBlank()) {
            return Result.failure(IllegalArgumentException("You must be signed in to create a farm"))
        }
        return farmRepository.createFarm(trimmedName, trimmedLocation, ownerId)
    }
}

class GetFarmUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(id: String): Result<Farm> = farmRepository.getFarm(id)
}

class UpdateFarmUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(farm: Farm): Result<Unit> {
        if (farm.name.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Farm name is required"))
        }
        if (farm.location.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Location is required"))
        }
        return farmRepository.updateFarm(farm.copy(name = farm.name.trim(), location = farm.location.trim()))
    }
}

class ArchiveFarmUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = farmRepository.archiveFarm(id)
}
