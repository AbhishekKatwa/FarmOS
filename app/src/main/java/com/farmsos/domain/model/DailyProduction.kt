package com.farmsos.domain.model

data class EggGrade(
    val id: String,
    val code: String,
    val displayName: String,
    val isActive: Boolean,
    val sortOrder: Int
)

data class EggGradeEntry(val eggGradeId: String, val quantity: Int)

data class MortalityRecord(
    val id: String = "",
    val productionDailyId: String = "",
    val farmId: String,
    val flockId: String,
    val date: String,
    val mortalityCount: Int,
    val cause: String = "",
    val remarks: String = ""
)

data class DailyProduction(
    val id: String = "",
    val farmId: String,
    val shedId: String,
    val flockId: String,
    val date: String,
    val openingLiveBirds: Int,
    val mortality: Int,
    val culls: Int,
    val closingLiveBirds: Int = openingLiveBirds - mortality - culls,
    val eggsCollected: Int,
    val brokenEggs: Int,
    val dirtyEggs: Int,
    val usableEggs: Int,
    val rejectedEggs: Int,
    val feedConsumedKg: Double,
    val remarks: String = "",
    val enteredBy: String = "",
    val eggGrades: List<EggGradeEntry> = emptyList(),
    val mortalityRecord: MortalityRecord? = null
)

data class ProductionMetrics(
    val averageLiveBirds: Double,
    val henDayPercent: Double?,
    val eggsPerBird: Double?,
    val feedPerBirdKg: Double?,
    val feedPerEggKg: Double?
)

object ProductionCalculator {
    fun closingLiveBirds(opening: Int, mortality: Int, culls: Int): Int = opening - mortality - culls

    fun metrics(production: DailyProduction): ProductionMetrics {
        val average = (production.openingLiveBirds + production.closingLiveBirds) / 2.0
        return ProductionMetrics(
            averageLiveBirds = average,
            henDayPercent = average.takeIf { it > 0 }?.let { production.eggsCollected / it * 100 },
            eggsPerBird = average.takeIf { it > 0 }?.let { production.eggsCollected / it },
            feedPerBirdKg = average.takeIf { it > 0 }?.let { production.feedConsumedKg / it },
            feedPerEggKg = production.eggsCollected.takeIf { it > 0 }?.let { production.feedConsumedKg / it }
        )
    }
}
