package com.farmsos.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true,
    val role: UserRole = UserRole.WORKER
)

enum class UserRole {
    OWNER,
    MANAGER,
    WORKER
}
