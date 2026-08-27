package com.farmsos.domain.model

data class Profile(
    val id: String,
    val email: String,
    val fullName: String,
    val createdAt: Long,
    val updatedAt: Long
)
