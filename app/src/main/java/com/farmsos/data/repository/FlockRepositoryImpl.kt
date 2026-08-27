package com.farmsos.data.repository

import com.farmsos.core.logging.AppLogger
import com.farmsos.data.mapper.toDomain
import com.farmsos.data.mapper.toInsertDto
import com.farmsos.data.remote.dto.FlockDto
import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.repository.FlockRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlockRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val logger: AppLogger
) : FlockRepository {

    override suspend fun listByFarm(farmId: String): Result<List<Flock>> {
        return runCatching {
            postgrest["flocks"].select {
                FlockDto::farmId eq farmId
            }.decodeList<FlockDto>().map { it.toDomain() }
        }.onFailure { logger.e("Failed to list flocks: ${it.message}", it) }
    }

    override suspend fun listByShed(shedId: String): Result<List<Flock>> {
        return runCatching {
            postgrest["flocks"].select {
                FlockDto::shedId eq shedId
            }.decodeList<FlockDto>().map { it.toDomain() }
        }.onFailure { logger.e("Failed to list flocks by shed: ${it.message}", it) }
    }

    override suspend fun getFlock(id: String): Result<Flock> {
        return runCatching {
            postgrest["flocks"].select {
                FlockDto::id eq id
            }.decodeSingle<FlockDto>().toDomain()
        }.onFailure { logger.e("Failed to load flock: ${it.message}", it) }
    }

    override suspend fun createFlock(flock: Flock): Result<Flock> {
        return runCatching {
            postgrest["flocks"].insert(flock.toInsertDto()) {
                select()
            }.decodeSingle<FlockDto>().toDomain()
        }.onFailure { logger.e("Failed to create flock: ${it.message}", it) }
    }

    override suspend fun updateFlock(flock: Flock): Result<Flock> {
        return runCatching {
            postgrest["flocks"].update({
                FlockDto::flockCode setTo flock.flockCode
                FlockDto::breed setTo flock.breed
                FlockDto::strain setTo flock.strain
                FlockDto::placementDate setTo flock.placementDate
                FlockDto::initialBirds setTo flock.initialBirds
                FlockDto::currentLiveBirds setTo flock.currentLiveBirds
                FlockDto::status setTo flock.status.name
                FlockDto::targetProduction setTo flock.targetProduction
                FlockDto::notes setTo flock.notes
            }) {
                filter {
                    FlockDto::id eq flock.id
                }
                select()
            }.decodeSingle<FlockDto>().toDomain()
        }.onFailure { logger.e("Failed to update flock: ${it.message}", it) }
    }

    override suspend fun archiveFlock(id: String): Result<Unit> {
        return runCatching {
            postgrest["flocks"].update({
                FlockDto::status setTo FlockStatus.CLOSED.name
            }) {
                filter {
                    FlockDto::id eq id
                }
            }
            Unit
        }.onFailure { logger.e("Failed to archive flock: ${it.message}", it) }
    }
}
