package com.farmsos.data.repository

import com.farmsos.core.logging.AppLogger
import com.farmsos.data.mapper.toDomain
import com.farmsos.data.remote.dto.ProfileDto
import com.farmsos.domain.model.Profile
import com.farmsos.domain.repository.ProfileRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val logger: AppLogger
) : ProfileRepository {

    override suspend fun getProfile(userId: String): Result<Profile> {
        return runCatching {
            postgrest["profiles"].select {
                filter { ProfileDto::id eq userId }
            }.decodeSingle<ProfileDto>().toDomain()
        }.onFailure { logger.e("Failed to load profile: ${it.message}", it) }
    }

    override suspend fun updateDisplayName(userId: String, fullName: String): Result<Profile> {
        return runCatching {
            postgrest["profiles"].update(
                {
                    ProfileDto::fullName setTo fullName
                }
            ) {
                filter {
                    ProfileDto::id eq userId
                }
            }.decodeSingle<ProfileDto>().toDomain()
        }.onFailure { logger.e("Failed to update profile: ${it.message}", it) }
    }
}
