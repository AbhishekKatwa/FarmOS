package com.farmsos.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true,
    val role: UserRole = UserRole.FARMER
)

enum class UserRole {
    FARMER,
    ADMIN,
    SUPERVISOR
}