package com.farmsos.domain.model

enum class FlockStatus {
    PLANNED,
    ACTIVE,
    DEPLETED,
    CLOSED
}

data class Flock(
    val id: String,
    val farmId: String,
    val shedId: String,
    val flockCode: String,
    val breed: String,
    val strain: String,
    val placementDate: String,
    val initialBirds: Int,
    val currentLiveBirds: Int,
    val status: FlockStatus,
    val targetProduction: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class FlockAge(
    val days: Int,
    val weeks: Int,
    val productionWeek: Int
)
