package com.farmsos.domain.usecase.farm

import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockAge
import com.farmsos.domain.model.FlockAgeCalculator
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.repository.FlockRepository
import com.farmsos.domain.repository.ShedRepository
import javax.inject.Inject

class ListFlocksUseCase @Inject constructor(
    private val flockRepository: FlockRepository
) {
    suspend operator fun invoke(farmId: String): Result<List<Flock>> = flockRepository.listByFarm(farmId)
}

class GetFlockUseCase @Inject constructor(
    private val flockRepository: FlockRepository
) {
    suspend operator fun invoke(id: String): Result<Flock> = flockRepository.getFlock(id)
}

class CalculateFlockAgeUseCase @Inject constructor() {
    operator fun invoke(placementDate: String, nowMillis: Long = System.currentTimeMillis()): FlockAge =
        FlockAgeCalculator.calculate(placementDate, nowMillis)
}

class CreateFlockUseCase @Inject constructor(
    private val shedRepository: ShedRepository,
    private val flockRepository: FlockRepository
) {
    suspend operator fun invoke(flock: Flock): Result<Flock> {
        val error = validate(flock) ?: return persist(flock)
        return Result.failure(IllegalArgumentException(error))
    }

    private suspend fun persist(flock: Flock): Result<Flock> {
        val shed = shedRepository.getShed(flock.shedId).getOrElse {
            return Result.failure(IllegalArgumentException("Flock must belong to a valid shed"))
        }
        if (!shed.isActive) {
            return Result.failure(IllegalArgumentException("Flock must belong to an active shed"))
        }
        if (shed.farmId != flock.farmId) {
            return Result.failure(IllegalArgumentException("Shed must belong to the current farm"))
        }
        return flockRepository.createFlock(
            flock.copy(
                flockCode = flock.flockCode.trim(),
                breed = flock.breed.trim(),
                strain = flock.strain.trim(),
                notes = flock.notes.trim(),
                targetProduction = flock.targetProduction.trim()
            )
        )
    }
}

class UpdateFlockUseCase @Inject constructor(
    private val shedRepository: ShedRepository,
    private val flockRepository: FlockRepository
) {
    suspend operator fun invoke(flock: Flock): Result<Flock> {
        validate(flock)?.let { return Result.failure(IllegalArgumentException(it)) }
        val shed = shedRepository.getShed(flock.shedId).getOrElse {
            return Result.failure(IllegalArgumentException("Flock must belong to a valid shed"))
        }
        if (shed.farmId != flock.farmId) {
            return Result.failure(IllegalArgumentException("Shed must belong to the current farm"))
        }
        return flockRepository.updateFlock(flock)
    }
}

class ArchiveFlockUseCase @Inject constructor(
    private val flockRepository: FlockRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = flockRepository.archiveFlock(id)
}

internal fun validate(flock: Flock): String? {
    if (flock.flockCode.trim().isBlank()) return "Flock code is required"
    if (flock.farmId.isBlank()) return "Farm is required"
    if (flock.shedId.isBlank()) return "Shed is required"
    if (flock.initialBirds <= 0) return "Initial birds must be greater than 0"
    if (flock.currentLiveBirds < 0) return "Current live birds cannot be negative"
    if (flock.currentLiveBirds > flock.initialBirds) return "Current live birds cannot exceed initial birds"
    if (!FlockAgeCalculator.isValidPlacementDate(flock.placementDate)) return "Placement date is not valid"
    if (flock.status == FlockStatus.ACTIVE && flock.currentLiveBirds == 0) {
        return "Active flocks must have live birds"
    }
    return null
}
