package com.farmsos.domain.model

/**
 * Farm-scoped membership. Authorization is per farm, not global.
 */
data class FarmMember(
    val farmId: String,
    val userId: String,
    val role: UserRole,
    val createdAt: Long
)
