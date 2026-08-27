package com.farmsos.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farms")
data class Farm(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val ownerId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true,
    val poultryCount: Int = 0,
    val poultryType: PoultryType = PoultryType.LAYER
)

enum class PoultryType {
    LAYER,
    BROILER,
    DUAL_PURPOSE
}