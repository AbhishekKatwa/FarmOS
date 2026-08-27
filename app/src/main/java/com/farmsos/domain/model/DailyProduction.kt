package com.farmsos.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class EggGrade(
    val id: String,
    val code: String,
    val displayName: String,
    val isActive: Boolean,
    val sortOrder: Int
)

@Entity(tableName = "egg_grade_entries")
data class EggGradeEntry(
    @PrimaryKey(autoGenerate = true) val localEntryId: Int = 0,
    val productionDailyLocalId: String,
    val eggGradeId: String,
    val quantity: Int
)

@Entity(tableName = "mortality_records")
data class MortalityRecord(
    @PrimaryKey override val localId: String = "",
    val id: String = "",
    val productionDailyId: String = "",
    val farmId: String,
    val flockId: String,
    val date: String,
    val mortalityCount: Int,
    val cause: String = "",
    val remarks: String = "",
    override val serverId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.PENDING,
    override val syncAttempts: Int = 0,
    override val lastSyncError: String? = null,
    override val idempotencyKey: String = "",
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : Syncable

@Entity(tableName = "production_daily")
data class DailyProduction(
    @PrimaryKey override val localId: String = "",
    val id: String = "",
    val farmId: String,
    val shedId: String,
    val flockId: String,
    val date: String,
    val openingLiveBirds: Int,
    val mortality: Int,
    val culls: Int,
    val closingLiveBirds: Int,
    val eggsCollected: Int,
    val brokenEggs: Int,
    val dirtyEggs: Int,
    val usableEggs: Int,
    val rejectedEggs: Int,
    val feedConsumedKg: Double,
    val remarks: String = "",
    val enteredBy: String = "",
    override val serverId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.PENDING,
    override val syncAttempts: Int = 0,
    override val lastSyncError: String? = null,
    override val idempotencyKey: String = "",
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : Syncable {
    @androidx.room.Ignore
    var eggGrades: List<EggGradeEntry> = emptyList()

    @androidx.room.Ignore
    var mortalityRecord: MortalityRecord? = null
}

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
