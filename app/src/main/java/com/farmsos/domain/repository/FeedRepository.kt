package com.farmsos.domain.repository

import com.farmsos.domain.model.*

interface FeedRepository {
    suspend fun items(farmId: String): Result<List<FeedItem>>
    suspend fun purchases(itemId: String): Result<List<FeedPurchase>>
    suspend fun consumption(itemId: String): Result<List<FeedConsumption>>
    suspend fun adjustments(itemId: String): Result<List<FeedAdjustment>>
    suspend fun addItem(item: FeedItem): Result<FeedItem>
    suspend fun addPurchase(value: FeedPurchase): Result<Unit>
    suspend fun addConsumption(value: FeedConsumption): Result<Unit>
    suspend fun addAdjustment(value: FeedAdjustment): Result<Unit>
    suspend fun syncPending(): Result<Unit>
}
