package com.farmsos.data.repository

import com.farmsos.core.logging.AppLogger
import com.farmsos.data.mapper.toDomain
import com.farmsos.data.remote.dto.ShedDto
import com.farmsos.data.remote.dto.ShedInsertDto
import com.farmsos.domain.model.Shed
import com.farmsos.domain.repository.ShedRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShedRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val logger: AppLogger
) : ShedRepository {

    override suspend fun listByFarm(farmId: String, includeArchived: Boolean): Result<List<Shed>> {
        return runCatching {
            val rows = postgrest["sheds"].select {
                ShedDto::farmId eq farmId
            }.decodeList<ShedDto>().map { it.toDomain() }
            if (includeArchived) rows else rows.filter { it.isActive }
        }.onFailure { logger.e("Failed to list sheds: ${it.message}", it) }
    }

    override suspend fun getShed(id: String): Result<Shed> {
        return runCatching {
            postgrest["sheds"].select {
                ShedDto::id eq id
            }.decodeSingle<ShedDto>().toDomain()
        }.onFailure { logger.e("Failed to load shed: ${it.message}", it) }
    }

    override suspend fun createShed(farmId: String, name: String, capacity: Int?, notes: String): Result<Shed> {
        return runCatching {
            postgrest["sheds"].insert(
                ShedInsertDto(farmId = farmId, name = name, capacity = capacity, notes = notes)
            ) {
                select()
            }.decodeSingle<ShedDto>().toDomain()
        }.onFailure { logger.e("Failed to create shed: ${it.message}", it) }
    }

    override suspend fun updateShed(shed: Shed): Result<Shed> {
        return runCatching {
            postgrest["sheds"].update({
                ShedDto::name setTo shed.name
                ShedDto::capacity setTo shed.capacity
                ShedDto::notes setTo shed.notes
            }) {
                filter {
                    ShedDto::id eq shed.id
                }
                select()
            }.decodeSingle<ShedDto>().toDomain()
        }.onFailure { logger.e("Failed to update shed: ${it.message}", it) }
    }

    override suspend fun archiveShed(id: String): Result<Unit> {
        return runCatching {
            postgrest["sheds"].update({
                ShedDto::isActive setTo false
            }) {
                filter {
                    ShedDto::id eq id
                }
            }
            Unit
        }.onFailure { logger.e("Failed to archive shed: ${it.message}", it) }
    }
}
