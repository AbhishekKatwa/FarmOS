package com.farmsos.data.repository

import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.FeedRepository
import com.farmsos.data.local.OperationalDao
import com.farmsos.worker.SyncScheduler
import io.github.jan.supabase.postgrest.Postgrest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val db: Postgrest,
    private val dao: OperationalDao,
    private val syncScheduler: SyncScheduler
) : FeedRepository {

    private fun FeedItemDto.domain() = FeedItem(
        id.orEmpty(),
        farmId,
        name,
        FeedType.valueOf(feedType),
        unit,
        openingQuantityKg,
        openingCostPerKg,
        openingDate.take(10),
        isActive
    )

    private fun FeedPurchaseDto.domain() = FeedPurchase(
        localId = UUID.randomUUID().toString(),
        id = id.orEmpty(),
        feedItemId = feedItemId,
        farmId = farmId,
        supplier = supplier,
        quantityKg = quantityKg,
        unit = unit,
        pricePerKg = pricePerKg,
        batch = batch,
        purchaseDate = purchaseDate.take(10),
        expiryDate = expiryDate?.take(10),
        remarks = remarks,
        serverId = id,
        syncStatus = SyncStatus.SYNCED,
        idempotencyKey = idempotencyKey ?: generateIdempotencyKey(),
        createdAt = updatedAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )

    private fun FeedConsumptionDto.domain() = FeedConsumption(
        localId = UUID.randomUUID().toString(),
        id = id.orEmpty(),
        feedItemId = feedItemId,
        farmId = farmId,
        flockId = flockId,
        quantityKg = quantityKg,
        consumedDate = consumedDate.take(10),
        remarks = remarks,
        serverId = id,
        syncStatus = SyncStatus.SYNCED,
        idempotencyKey = idempotencyKey ?: generateIdempotencyKey(),
        createdAt = updatedAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )

    private fun FeedAdjustmentDto.domain() = FeedAdjustment(
        localId = UUID.randomUUID().toString(),
        id = id.orEmpty(),
        feedItemId = feedItemId,
        farmId = farmId,
        quantityKg = quantityKg,
        adjustmentDate = adjustmentDate.take(10),
        reason = reason,
        allowNegativeStock = allowNegativeStock,
        serverId = id,
        syncStatus = SyncStatus.SYNCED,
        idempotencyKey = idempotencyKey ?: generateIdempotencyKey(),
        createdAt = updatedAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )

    override suspend fun items(farmId: String) = runCatching {
        db["feed_items"].select { filter { FeedItemDto::farmId eq farmId } }.decodeList<FeedItemDto>()
            .map { it.domain() }
    }

    override suspend fun purchases(itemId: String) = runCatching {
        db["feed_purchases"].select { filter { FeedPurchaseDto::feedItemId eq itemId } }
            .decodeList<FeedPurchaseDto>().map { it.domain() }
    }

    override suspend fun consumption(itemId: String) = runCatching {
        db["feed_consumption"].select { filter { FeedConsumptionDto::feedItemId eq itemId } }
            .decodeList<FeedConsumptionDto>().map { it.domain() }
    }

    override suspend fun adjustments(itemId: String) = runCatching {
        db["feed_adjustments"].select { filter { FeedAdjustmentDto::feedItemId eq itemId } }
            .decodeList<FeedAdjustmentDto>().map { it.domain() }
    }

    override suspend fun addItem(item: FeedItem) = runCatching {
        db["feed_items"].insert(
            FeedItemDto(
                farmId = item.farmId,
                name = item.name,
                feedType = item.feedType.name,
                unit = item.unit,
                openingQuantityKg = item.openingQuantityKg,
                openingCostPerKg = item.openingCostPerKg,
                openingDate = item.openingDate
            )
        ) { select() }.decodeSingle<FeedItemDto>().domain()
    }

    override suspend fun addPurchase(value: FeedPurchase) = runCatching {
        dao.insertFeedPurchase(value)
        syncScheduler.scheduleSync()
    }

    override suspend fun addConsumption(value: FeedConsumption) = runCatching {
        dao.insertFeedConsumption(value)
        syncScheduler.scheduleSync()
    }

    override suspend fun addAdjustment(value: FeedAdjustment) = runCatching {
        dao.insertFeedAdjustment(value)
        syncScheduler.scheduleSync()
    }

    override suspend fun syncPending(): Result<Unit> = runCatching {
        val pendingPurchases = dao.getPendingFeedPurchases()
        pendingPurchases.forEach { purchase ->
            try {
                dao.updateFeedPurchase(purchase.copy(syncStatus = SyncStatus.SYNCING))
                val saved = db["feed_purchases"].upsert(
                    FeedPurchaseDto(
                        feedItemId = purchase.feedItemId,
                        farmId = purchase.farmId,
                        supplier = purchase.supplier,
                        quantityKg = purchase.quantityKg,
                        unit = purchase.unit,
                        pricePerKg = purchase.pricePerKg,
                        batch = purchase.batch,
                        purchaseDate = purchase.purchaseDate,
                        expiryDate = purchase.expiryDate,
                        remarks = purchase.remarks,
                        idempotencyKey = purchase.idempotencyKey,
                        updatedAt = purchase.updatedAt
                    )
                ) { select() }.decodeSingle<FeedPurchaseDto>()
                dao.updateFeedPurchase(
                    purchase.copy(
                        serverId = saved.id,
                        syncStatus = SyncStatus.SYNCED,
                        syncAttempts = purchase.syncAttempts + 1
                    )
                )
            } catch (e: Exception) {
                dao.updateFeedPurchase(
                    purchase.copy(
                        syncStatus = SyncStatus.FAILED,
                        syncAttempts = purchase.syncAttempts + 1,
                        lastSyncError = e.message
                    )
                )
            }
        }

        val pendingConsumption = dao.getPendingFeedConsumption()
        pendingConsumption.forEach { consumption ->
            try {
                dao.updateFeedConsumption(consumption.copy(syncStatus = SyncStatus.SYNCING))
                val saved = db["feed_consumption"].upsert(
                    FeedConsumptionDto(
                        feedItemId = consumption.feedItemId,
                        farmId = consumption.farmId,
                        flockId = consumption.flockId,
                        quantityKg = consumption.quantityKg,
                        consumedDate = consumption.consumedDate,
                        remarks = consumption.remarks,
                        idempotencyKey = consumption.idempotencyKey,
                        updatedAt = consumption.updatedAt
                    )
                ) { select() }.decodeSingle<FeedConsumptionDto>()
                dao.updateFeedConsumption(
                    consumption.copy(
                        serverId = saved.id,
                        syncStatus = SyncStatus.SYNCED,
                        syncAttempts = consumption.syncAttempts + 1
                    )
                )
            } catch (e: Exception) {
                dao.updateFeedConsumption(
                    consumption.copy(
                        syncStatus = SyncStatus.FAILED,
                        syncAttempts = consumption.syncAttempts + 1,
                        lastSyncError = e.message
                    )
                )
            }
        }
    }
}
