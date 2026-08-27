package com.farmsos.data.mapper

import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*

fun EggGradeDto.toDomain() = EggGrade(id.orEmpty(), code, displayName, isActive, sortOrder)
fun DailyProductionDto.toDomain(entries: List<EggGradeEntry> = emptyList(), mortalityRecord: MortalityRecord? = null) = DailyProduction(
    id.orEmpty(), farmId, shedId, flockId, date.take(10), openingLiveBirds, mortality, culls,
    closingLiveBirds ?: ProductionCalculator.closingLiveBirds(openingLiveBirds, mortality, culls), eggsCollected, brokenEggs,
    dirtyEggs, usableEggs, rejectedEggs, feedConsumedKg, remarks, enteredBy.orEmpty(), entries, mortalityRecord)
fun DailyProduction.toWriteDto() = DailyProductionWriteDto(farmId, shedId, flockId, date, openingLiveBirds, mortality, culls, eggsCollected, brokenEggs, dirtyEggs, usableEggs, rejectedEggs, feedConsumedKg, remarks)
fun MortalityRecord.toDto(productionId: String) = MortalityRecordDto(productionId = productionId, farmId = farmId, flockId = flockId, date = date, mortalityCount = mortalityCount, cause = cause, remarks = remarks)
fun MortalityRecordDto.toDomain() = MortalityRecord(id.orEmpty(), productionDailyId, farmId, flockId, date.take(10), mortalityCount, cause, remarks)
