package com.farmsos.domain.model

data class Shed(
    val id: String,
    val farmId: String,
    val name: String,
    val capacity: Int?,
    val notes: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
