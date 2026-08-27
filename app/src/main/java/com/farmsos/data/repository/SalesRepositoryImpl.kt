package com.farmsos.data.repository

import com.farmsos.data.local.OperationalDao
import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.SalesRepository
import com.farmsos.worker.SyncScheduler
import io.github.jan.supabase.postgrest.Postgrest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepositoryImpl @Inject constructor(
    private val db: Postgrest,
    private val dao: OperationalDao,
    private val syncScheduler: SyncScheduler
) : SalesRepository {

    private fun BuyerDto.d() = Buyer(
        id.orEmpty(),
        farmId,
        name,
        code,
        phone,
        address,
        location,
        paymentTerms,
        creditLimit,
        openingBalance,
        active,
        notes
    )

    private fun DispatchDto.d() = Dispatch(
        localId = UUID.randomUUID().toString(),
        id = id.orEmpty(),
        farmId = farmId,
        buyerId = buyerId,
        flockId = flockId,
        date = date,
        transport = transport,
        loadingCharges = loadingCharges,
        otherCharges = otherCharges,
        invoiceReference = invoiceReference,
        vehicle = vehicle,
        driver = driver,
        remarks = remarks,
        allowOversell = allowOversell,
        serverId = id,
        syncStatus = SyncStatus.SYNCED,
        idempotencyKey = idempotencyKey ?: generateIdempotencyKey(),
        createdAt = updatedAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )

    private fun BuyerPaymentDto.d() = BuyerPayment(
        localId = UUID.randomUUID().toString(),
        farmId = farmId,
        buyerId = buyerId,
        date = date,
        amount = amount,
        reference = reference,
        method = method,
        notes = notes,
        serverId = null,
        syncStatus = SyncStatus.SYNCED,
        idempotencyKey = idempotencyKey ?: generateIdempotencyKey(),
        createdAt = updatedAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )

    override suspend fun buyers(farmId: String) = runCatching {
        db["buyers"].select { filter { BuyerDto::farmId eq farmId } }.decodeList<BuyerDto>().map { it.d() }
    }

    override suspend fun dispatches(buyerId: String) = runCatching {
        db["dispatches"].select { filter { DispatchDto::buyerId eq buyerId } }.decodeList<DispatchDto>()
            .map { it.d() }
    }

    override suspend fun balances(farmId: String) = runCatching {
        db["buyer_balances"].select { filter { BuyerBalanceDto::farmId eq farmId } }
            .decodeList<BuyerBalanceDto>()
            .map { BuyerBalance(it.buyerId, it.farmId, it.name, it.closingBalance) }
    }

    override suspend fun aging(farmId: String) = runCatching {
        db["buyer_outstanding_aging"].select { filter { OutstandingAgingDto::farmId eq farmId } }
            .decodeList<OutstandingAgingDto>().map {
                OutstandingAging(
                    it.buyerId,
                    it.farmId,
                    it.current,
                    it.days1To7,
                    it.days8To30,
                    it.days31To60,
                    it.days60Plus
                )
            }
    }

    override suspend fun addBuyer(v: Buyer) = runCatching {
        db["buyers"].insert(
            BuyerDto(
                farmId = v.farmId,
                name = v.name,
                code = v.code,
                phone = v.phone,
                address = v.address,
                location = v.location,
                paymentTerms = v.paymentTerms,
                creditLimit = v.creditLimit,
                openingBalance = v.openingBalance,
                active = v.active,
                notes = v.notes
            )
        ) { select() }.decodeSingle<BuyerDto>().d()
    }

    override suspend fun createDispatch(h: Dispatch, items: List<DispatchItem>) = runCatching {
        require(items.isNotEmpty()) { "Dispatch requires at least one line item" }
        require(items.all { it.trays > 0 && it.eggs > 0 && it.rate >= 0 }) { "Dispatch quantities and rates are invalid" }

        // Save locally
        dao.insertDispatch(h)
        dao.insertDispatchItems(items.map { it.copy(dispatchLocalId = h.localId) })

        syncScheduler.scheduleSync()
        h
    }

    override suspend fun recordPayment(v: BuyerPayment) = runCatching {
        require(v.amount > 0) { "Payment must be greater than zero" }
        dao.insertBuyerPayment(v)
        syncScheduler.scheduleSync()
    }

    override suspend fun syncPending(): Result<Unit> = runCatching {
        val pendingDispatches = dao.getPendingDispatches()
        pendingDispatches.forEach { dispatch ->
            try {
                dao.updateDispatch(dispatch.copy(syncStatus = SyncStatus.SYNCING))

                // Conflict handling
                val serverRecords = db["dispatches"].select {
                    filter { DispatchDto::idempotencyKey eq dispatch.idempotencyKey }
                }.decodeList<DispatchDto>()

                val serverRecord = serverRecords.firstOrNull()
                if (serverRecord != null && (serverRecord.updatedAt ?: 0) > dispatch.updatedAt) {
                    dao.updateDispatch(dispatch.copy(
                        serverId = serverRecord.id,
                        syncStatus = SyncStatus.SYNCED
                    ))
                    return@forEach
                }

                val saved = db["dispatches"].upsert(
                    DispatchDto(
                        farmId = dispatch.farmId,
                        buyerId = dispatch.buyerId,
                        flockId = dispatch.flockId,
                        date = dispatch.date,
                        transport = dispatch.transport,
                        loadingCharges = dispatch.loadingCharges,
                        otherCharges = dispatch.otherCharges,
                        invoiceReference = dispatch.invoiceReference,
                        vehicle = dispatch.vehicle,
                        driver = dispatch.driver,
                        remarks = dispatch.remarks,
                        allowOversell = dispatch.allowOversell,
                        idempotencyKey = dispatch.idempotencyKey,
                        updatedAt = dispatch.updatedAt
                    )
                ) { select() }.decodeSingle<DispatchDto>()

                dao.updateDispatch(
                    dispatch.copy(
                        serverId = saved.id,
                        syncStatus = SyncStatus.SYNCED,
                        syncAttempts = dispatch.syncAttempts + 1
                    )
                )
            } catch (e: Exception) {
                dao.updateDispatch(
                    dispatch.copy(
                        syncStatus = SyncStatus.FAILED,
                        syncAttempts = dispatch.syncAttempts + 1,
                        lastSyncError = e.message
                    )
                )
            }
        }

        val pendingPayments = dao.getPendingBuyerPayments()
        pendingPayments.forEach { payment ->
            try {
                dao.updateBuyerPayment(payment.copy(syncStatus = SyncStatus.SYNCING))
                db["buyer_payments"].upsert(
                    BuyerPaymentDto(
                        farmId = payment.farmId,
                        buyerId = payment.buyerId,
                        date = payment.date,
                        amount = payment.amount,
                        reference = payment.reference,
                        method = payment.method,
                        notes = payment.notes,
                        idempotencyKey = payment.idempotencyKey,
                        updatedAt = payment.updatedAt
                    )
                )
                dao.updateBuyerPayment(
                    payment.copy(
                        syncStatus = SyncStatus.SYNCED,
                        syncAttempts = payment.syncAttempts + 1
                    )
                )
            } catch (e: Exception) {
                dao.updateBuyerPayment(
                    payment.copy(
                        syncStatus = SyncStatus.FAILED,
                        syncAttempts = payment.syncAttempts + 1,
                        lastSyncError = e.message
                    )
                )
            }
        }
    }
}
