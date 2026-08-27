package com.farmsos.domain.model

import java.util.UUID

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}

interface Syncable {
    val localId: String
    val serverId: String?
    val syncStatus: SyncStatus
    val syncAttempts: Int
    val lastSyncError: String?
    val idempotencyKey: String
    val createdAt: Long
    val updatedAt: Long
}

fun generateIdempotencyKey(): String = UUID.randomUUID().toString()
