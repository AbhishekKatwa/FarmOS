package com.farmsos.domain.usecase.farm

import com.farmsos.domain.model.DailyProduction
import com.farmsos.domain.model.ProductionCalculator
import com.farmsos.domain.repository.FlockRepository
import com.farmsos.domain.repository.ProductionRepository
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ListEggGradesUseCase @Inject constructor(private val repository: ProductionRepository) { suspend operator fun invoke() = repository.listGrades() }
class ListProductionUseCase @Inject constructor(private val repository: ProductionRepository) { suspend operator fun invoke(flockId: String) = repository.listByFlock(flockId) }
class GetProductionUseCase @Inject constructor(private val repository: ProductionRepository) { suspend operator fun invoke(id: String) = repository.get(id) }
class CreateProductionUseCase @Inject constructor(private val flocks: FlockRepository, private val repository: ProductionRepository) {
    suspend operator fun invoke(production: DailyProduction): Result<DailyProduction> = validateProduction(production) ?: run {
        val flock = flocks.getFlock(production.flockId).getOrElse { return Result.failure(IllegalArgumentException("Flock is required")) }
        if (flock.farmId != production.farmId || flock.shedId != production.shedId) return Result.failure(IllegalArgumentException("Flock must belong to the selected farm and shed"))
        repository.create(production.copy(remarks = production.remarks.trim()))
    }
}
class UpdateProductionUseCase @Inject constructor(private val repository: ProductionRepository) {
    suspend operator fun invoke(production: DailyProduction): Result<DailyProduction> = validateProduction(production) ?: repository.update(production.copy(remarks = production.remarks.trim()))
}

private fun validateProduction(production: DailyProduction): Result<DailyProduction>? {
    val message = when {
        production.farmId.isBlank() || production.shedId.isBlank() || production.flockId.isBlank() -> "Farm, shed, and flock are required"
        !isValidProductionDate(production.date) -> "Date must be YYYY-MM-DD"
        production.openingLiveBirds < 0 -> "Opening live birds cannot be negative"
        production.mortality < 0 -> "Mortality cannot be negative"
        production.culls < 0 -> "Culls cannot be negative"
        production.mortality + production.culls > production.openingLiveBirds -> "Mortality and culls cannot exceed available birds"
        listOf(production.eggsCollected, production.brokenEggs, production.dirtyEggs, production.usableEggs, production.rejectedEggs).any { it < 0 } -> "Egg counts cannot be negative"
        production.feedConsumedKg < 0 -> "Feed consumed cannot be negative"
        production.eggGrades.any { it.quantity < 0 } -> "Egg grade quantities cannot be negative"
        production.mortalityRecord?.mortalityCount?.let { it != production.mortality } == true -> "Mortality entry must match daily mortality"
        else -> null
    }
    return message?.let { Result.failure(IllegalArgumentException(it)) }
}

private fun isValidProductionDate(value: String): Boolean = runCatching {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    formatter.parse(value)?.let { formatter.format(it) == value } == true
}.getOrDefault(false)
