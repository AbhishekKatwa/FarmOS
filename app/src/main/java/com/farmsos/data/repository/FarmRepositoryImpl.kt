package com.farmsos.data.repository

import com.farmsos.core.logging.AppLogger
import com.farmsos.data.mapper.toDomain
import com.farmsos.data.remote.dto.FarmDto
import com.farmsos.data.remote.dto.FarmInsertDto
import com.farmsos.domain.model.Farm
import com.farmsos.domain.repository.FarmRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val logger: AppLogger
) : FarmRepository {

    private val farms = MutableStateFlow<List<Farm>>(emptyList())

    override fun getAllFarms(): Flow<List<Farm>> = farms.asStateFlow()

    override suspend fun refreshFarms(): Result<List<Farm>> {
        return runCatching {
            val loaded = postgrest["farms"].select {
                FarmDto::isActive eq true
            }.decodeList<FarmDto>().map { it.toDomain() }
            farms.value = loaded
            loaded
        }.onFailure { logger.e("Failed to load farms: ${it.message}", it) }
    }

    override suspend fun getFarm(id: String): Result<Farm> {
        return runCatching {
            postgrest["farms"].select {
                FarmDto::id eq id
            }.decodeSingle<FarmDto>().toDomain()
        }.onFailure { logger.e("Failed to load farm: ${it.message}", it) }
    }

    override suspend fun getFarmsByOwner(ownerId: String): Result<List<Farm>> {
        return runCatching {
            postgrest["farms"].select {
                FarmDto::ownerId eq ownerId
            }.decodeList<FarmDto>().map { it.toDomain() }
        }.onFailure { logger.e("Failed to load farms by owner: ${it.message}", it) }
    }

    override suspend fun insertFarm(farm: Farm): Result<Unit> {
        return createFarm(farm.name, farm.location, farm.ownerId).map { }
    }

    override suspend fun createFarm(name: String, location: String, ownerId: String): Result<Farm> {
        return runCatching {
            val created = postgrest["farms"].insert(FarmInsertDto(name = name, location = location, ownerId = ownerId)) {
                select()
            }.decodeSingle<FarmDto>().toDomain()
            refreshFarms()
            created
        }.onFailure { logger.e("Failed to create farm: ${it.message}", it) }
    }

    override suspend fun updateFarm(farm: Farm): Result<Unit> {
        return runCatching {
            postgrest["farms"].update({
                FarmDto::name setTo farm.name
                FarmDto::location setTo farm.location
            }) {
                filter {
                    FarmDto::id eq farm.id
                }
            }
            refreshFarms()
            Unit
        }.onFailure { logger.e("Failed to update farm: ${it.message}", it) }
    }

    override suspend fun archiveFarm(id: String): Result<Unit> {
        return runCatching {
            postgrest["farms"].update({
                FarmDto::isActive setTo false
            }) {
                filter {
                    FarmDto::id eq id
                }
            }
            refreshFarms()
            Unit
        }.onFailure { logger.e("Failed to archive farm: ${it.message}", it) }
    }

    override suspend fun deleteFarm(id: String): Result<Unit> = archiveFarm(id)
}
