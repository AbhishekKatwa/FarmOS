package com.farmsos.domain.repository

import com.farmsos.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<Profile>
    suspend fun updateDisplayName(userId: String, fullName: String): Result<Profile>
}
