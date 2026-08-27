package com.farmsos.data.mapper

import com.farmsos.data.remote.dto.ProfileDto
import com.farmsos.domain.model.Profile
import com.farmsos.domain.model.User
import com.farmsos.domain.model.UserRole
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.jsonPrimitive

fun ProfileDto.toDomain(): Profile = Profile(
    id = id,
    email = email.orEmpty(),
    fullName = fullName.orEmpty(),
    createdAt = 0L,
    updatedAt = 0L
)

fun UserInfo.toDomainUser(profile: Profile?): User {
    val metadataName = userMetadata?.get("full_name")?.jsonPrimitive?.content
    return User(
        id = id,
        name = profile?.fullName?.ifBlank { null } ?: metadataName.orEmpty(),
        email = email ?: profile?.email.orEmpty(),
        createdAt = 0L,
        updatedAt = 0L,
        isActive = true,
        role = UserRole.WORKER
    )
}
