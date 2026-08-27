package com.farmsos.data.repository

import com.farmsos.core.logging.AppLogger
import com.farmsos.data.mapper.*
import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.ProductionRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val logger: AppLogger
) : ProductionRepository {
    override suspend fun listGrades() = runCatching {
        postgrest["production_egg_grades"].select { EggGradeDto::isActive eq true }
            .decodeList<EggGradeDto>().map { it.toDomain() }.sortedBy { it.sortOrder }
    }.onFailure { logger.e("Failed to load egg grades: ${it.message}", it) }

    override suspend fun listByFlock(flockId: String) = runCatching {
        postgrest["production_daily"].select { DailyProductionDto::flockId eq flockId }
            .decodeList<DailyProductionDto>().map { loadDetails(it) }.sortedByDescending { it.date }
    }.onFailure { logger.e("Failed to list daily production: ${it.message}", it) }

    override suspend fun get(id: String) = runCatching {
        val row = postgrest["production_daily"].select { DailyProductionDto::id eq id }.decodeSingle<DailyProductionDto>()
        loadDetails(row)
    }.onFailure { logger.e("Failed to load daily production: ${it.message}", it) }

    override suspend fun create(production: DailyProduction) = runCatching {
        val saved = postgrest["production_daily"].insert(production.toWriteDto()) { select() }.decodeSingle<DailyProductionDto>()
        saveChildren(saved.id.orEmpty(), production)
        get(saved.id.orEmpty()).getOrThrow()
    }.onFailure { logger.e("Failed to create daily production: ${it.message}", it) }

    override suspend fun update(production: DailyProduction) = runCatching {
        postgrest["production_daily"].update(production.toWriteDto()) {
            filter { DailyProductionDto::id eq production.id }; select()
        }.decodeSingle<DailyProductionDto>()
        postgrest["production_daily_egg_grade_entries"].delete { EggGradeEntryDto::productionDailyId eq production.id }
        postgrest["mortality_records"].delete { MortalityRecordDto::productionDailyId eq production.id }
        saveChildren(production.id, production)
        get(production.id).getOrThrow()
    }.onFailure { logger.e("Failed to update daily production: ${it.message}", it) }

    private suspend fun saveChildren(id: String, production: DailyProduction) {
        production.eggGrades.filter { it.quantity >= 0 }.forEach { entry ->
            postgrest["production_daily_egg_grade_entries"].insert(EggGradeEntryDto(id, entry.eggGradeId, entry.quantity))
        }
        production.mortalityRecord?.let { record ->
            postgrest["mortality_records"].insert(record.toDto(id))
        }
    }

    private suspend fun loadDetails(row: DailyProductionDto): DailyProduction {
        val id = row.id.orEmpty()
        val entries = postgrest["production_daily_egg_grade_entries"].select {
            EggGradeEntryDto::productionDailyId eq id
        }.decodeList<EggGradeEntryDto>().map { EggGradeEntry(it.eggGradeId, it.quantity) }
        val mortality = postgrest["mortality_records"].select {
            MortalityRecordDto::productionDailyId eq id
        }.decodeList<MortalityRecordDto>().firstOrNull()?.toDomain()
        return row.toDomain(entries, mortality)
    }
}
