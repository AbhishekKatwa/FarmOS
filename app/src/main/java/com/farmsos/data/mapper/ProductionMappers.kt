package com.farmsos.data.mapper

import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import java.util.UUID

fun EggGradeDto.toDomain() = EggGrade(id.orEmpty(), code, displayName, isActive, sortOrder)
fun DailyProductionDto.toDomain(entries: List<EggGradeEntry> = emptyList(), mortalityRecord: MortalityRecord? = null) = DailyProduction(
    localId = UUID.randomUUID().toString(),
    id = id.orEmpty(), farmId = farmId, shedId = shedId, flockId = flockId, date = date.take(10), openingLiveBirds = openingLiveBirds, mortality = mortality, culls = culls,
    closingLiveBirds = closingLiveBirds ?: ProductionCalculator.closingLiveBirds(openingLiveBirds, mortality, culls), eggsCollected = eggsCollected, brokenEggs = brokenEggs,
    dirtyEggs = dirtyEggs, usableEggs = usableEggs, rejectedEggs = rejectedEggs, feedConsumedKg = feedConsumedKg, remarks = remarks, enteredBy = enteredBy.orEmpty(),
    serverId = id, syncStatus = SyncStatus.SYNCED, idempotencyKey = idempotencyKey ?: generateIdempotencyKey(),
    createdAt = updatedAt ?: System.currentTimeMillis(),
    updatedAt = updatedAt ?: System.currentTimeMillis()
).apply {
    this.eggGrades = entries
    this.mortalityRecord = mortalityRecord
}
fun DailyProduction.toWriteDto() = DailyProductionWriteDto(farmId, shedId, flockId, date, openingLiveBirds, mortality, culls, eggsCollected, brokenEggs, dirtyEggs, usableEggs, rejectedEggs, feedConsumedKg, remarks, idempotencyKey, updatedAt)
fun MortalityRecord.toDto(productionId: String) = MortalityRecordDto(id = serverId, productionDailyId = productionId, farmId = farmId, flockId = flockId, date = date, mortalityCount = mortalityCount, cause = cause, remarks = remarks, idempotencyKey = idempotencyKey, updatedAt = updatedAt)
fun MortalityRecordDto.toDomain() = MortalityRecord(
    localId = UUID.randomUUID().toString(),
    id = id.orEmpty(), productionDailyId = productionDailyId, farmId = farmId, flockId = flockId, date = date.take(10), mortalityCount = mortalityCount, cause = cause, remarks = remarks, serverId = id, syncStatus = SyncStatus.SYNCED, idempotencyKey = idempotencyKey ?: generateIdempotencyKey(),
    createdAt = updatedAt ?: System.currentTimeMillis(),
    updatedAt = updatedAt ?: System.currentTimeMillis()
)
