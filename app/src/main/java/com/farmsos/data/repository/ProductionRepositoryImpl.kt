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
    private val dao: com.farmsos.data.local.OperationalDao,
    private val syncScheduler: com.farmsos.worker.SyncScheduler,
    private val logger: AppLogger
) : ProductionRepository {
    override suspend fun listGrades() = runCatching {
        postgrest["production_egg_grades"].select { filter {EggGradeDto::isActive eq true }}
            .decodeList<EggGradeDto>().map { it.toDomain() }.sortedBy { it.sortOrder }
    }.onFailure { logger.e("Failed to load egg grades: ${it.message}", it) }

    override fun observeByFlock(flockId: String): kotlinx.coroutines.flow.Flow<List<DailyProduction>> =
        dao.getProductionByFlock(flockId)

    override suspend fun listByFlock(flockId: String) = runCatching {
        // Fetch from remote and update local cache
        val remote = postgrest["production_daily"].select { filter {DailyProductionDto::flockId eq flockId }}
            .decodeList<DailyProductionDto>().map { loadDetails(it) }
        remote.forEach { dao.insertProduction(it) }
        remote.sortedByDescending { it.date }
    }.onFailure { logger.e("Failed to list daily production: ${it.message}", it) }

    override suspend fun get(id: String) = runCatching {
        val row = postgrest["production_daily"].select { filter {DailyProductionDto::id eq id }}.decodeSingle<DailyProductionDto>()
        loadDetails(row)
    }.onFailure { logger.e("Failed to load daily production: ${it.message}", it) }

    override suspend fun create(production: DailyProduction) = runCatching {
        // Save locally first
        dao.insertProduction(production)
        production.eggGrades.forEach { entry ->
            dao.insertEggGradeEntries(listOf(entry.copy(productionDailyLocalId = production.localId)))
        }
        production.mortalityRecord?.let { dao.insertMortality(it.copy(productionDailyId = production.localId)) }

        // Trigger sync
        syncScheduler.scheduleSync()
        production
    }.onFailure { logger.e("Failed to create daily production: ${it.message}", it) }

    override suspend fun update(production: DailyProduction) = runCatching {
        dao.updateProduction(production.copy(syncStatus = SyncStatus.PENDING, updatedAt = System.currentTimeMillis()))
        syncScheduler.scheduleSync()
        production
    }.onFailure { logger.e("Failed to update daily production: ${it.message}", it) }

    override suspend fun syncPending(): Result<Unit> = runCatching {
        val pending = dao.getPendingProduction()
        pending.forEach { prod ->
            val entries = dao.getEggGradeEntries(prod.localId).map { EggGradeEntry(productionDailyLocalId = prod.localId, eggGradeId = it.eggGradeId, quantity = it.quantity) }
            val mortality = dao.getMortalityByProduction(prod.localId)
            val fullProd = prod.copy().apply {
                this.eggGrades = entries
                this.mortalityRecord = mortality
            }

            try {
                dao.updateProduction(prod.copy(syncStatus = SyncStatus.SYNCING))
                
                // Conflict handling: Check if server has newer data
                val serverRecords = postgrest["production_daily"].select {
                    filter { DailyProductionDto::idempotencyKey eq prod.idempotencyKey }
                }.decodeList<DailyProductionDto>()
                
                val serverRecord = serverRecords.firstOrNull()
                if (serverRecord != null && (serverRecord.updatedAt ?: 0) > prod.updatedAt) {
                    // Server data is newer, resolve conflict by accepting server data
                    val resolved = serverRecord.toDomain(entries, mortality).let {
                        it.copy(
                            localId = prod.localId,
                            syncStatus = SyncStatus.SYNCED
                        ).apply {
                            this.eggGrades = it.eggGrades
                            this.mortalityRecord = it.mortalityRecord
                        }
                    }
                    dao.updateProduction(resolved)
                    return@forEach
                }

                val saved = postgrest["production_daily"].upsert(fullProd.toWriteDto()) {
                    select()
                }.decodeSingle<DailyProductionDto>()

                saveChildrenRemote(saved.id.orEmpty(), fullProd)

                dao.updateProduction(prod.copy(
                    serverId = saved.id,
                    syncStatus = SyncStatus.SYNCED,
                    syncAttempts = prod.syncAttempts + 1
                ))
            } catch (e: Exception) {
                dao.updateProduction(prod.copy(
                    syncStatus = SyncStatus.FAILED,
                    syncAttempts = prod.syncAttempts + 1,
                    lastSyncError = e.message
                ))
            }
        }
    }

    private suspend fun saveChildrenRemote(id: String, production: DailyProduction) {
        production.eggGrades.filter { it.quantity >= 0 }.forEach { entry ->
            postgrest["production_daily_egg_grade_entries"].upsert(EggGradeEntryDto(id, entry.eggGradeId, entry.quantity, idempotencyKey = "${id}_${entry.eggGradeId}"))
        }
        production.mortalityRecord?.let { record ->
            postgrest["mortality_records"].upsert(record.toDto(id))
        }
    }

    private suspend fun loadDetails(row: DailyProductionDto): DailyProduction {
        val id = row.id.orEmpty()
        val entries = postgrest["production_daily_egg_grade_entries"].select {
            filter {EggGradeEntryDto::productionDailyId eq id}
        }.decodeList<EggGradeEntryDto>().map { EggGradeEntry(productionDailyLocalId = "", eggGradeId = it.eggGradeId, quantity = it.quantity) }
        val mortality = postgrest["mortality_records"].select {
            filter {MortalityRecordDto::productionDailyId eq id}
        }.decodeList<MortalityRecordDto>().firstOrNull()?.toDomain()
        return row.toDomain(entries, mortality)
    }
}
