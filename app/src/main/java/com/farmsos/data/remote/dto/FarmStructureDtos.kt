package com.farmsos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FarmInsertDto(
    val name: String,
    val location: String = "",
    @SerialName("owner_id") val ownerId: String
)

@Serializable
data class ShedInsertDto(
    @SerialName("farm_id") val farmId: String,
    val name: String,
    val capacity: Int? = null,
    val notes: String = ""
)

@Serializable
data class FarmDto(
    val id: String? = null,
    val name: String,
    val location: String = "",
    @SerialName("owner_id") val ownerId: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ShedDto(
    val id: String? = null,
    @SerialName("farm_id") val farmId: String,
    val name: String,
    val capacity: Int? = null,
    val notes: String = "",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class FlockDto(
    val id: String? = null,
    @SerialName("farm_id") val farmId: String,
    @SerialName("shed_id") val shedId: String,
    @SerialName("flock_code") val flockCode: String,
    val breed: String = "",
    val strain: String = "",
    @SerialName("placement_date") val placementDate: String,
    @SerialName("initial_birds") val initialBirds: Int,
    @SerialName("current_live_birds") val currentLiveBirds: Int,
    val status: String,
    @SerialName("target_production") val targetProduction: String = "",
    val notes: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
