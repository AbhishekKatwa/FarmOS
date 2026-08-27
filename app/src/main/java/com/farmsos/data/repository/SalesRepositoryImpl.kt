package com.farmsos.data.repository

import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.SalesRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepositoryImpl @Inject constructor(private val db: Postgrest) : SalesRepository {
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
    );

    private fun DispatchDto.d() = Dispatch(
        id.orEmpty(),
        farmId,
        buyerId,
        flockId,
        date,
        transport,
        loadingCharges,
        otherCharges,
        invoiceReference,
        vehicle,
        driver,
        remarks,
        allowOversell
    )

    override suspend fun buyers(farmId: String) = runCatching {
        db["buyers"].select { filter {BuyerDto::farmId eq farmId }}.decodeList<BuyerDto>().map { it.d() }
    }

    override suspend fun dispatches(buyerId: String) = runCatching {
        db["dispatches"].select { filter {DispatchDto::buyerId eq buyerId }}.decodeList<DispatchDto>()
            .map { it.d() }
    }

    override suspend fun balances(farmId: String) = runCatching {
        db["buyer_balances"].select { filter {BuyerBalanceDto::farmId eq farmId }}
            .decodeList<BuyerBalanceDto>()
            .map { BuyerBalance(it.buyerId, it.farmId, it.name, it.closingBalance) }
    }

    override suspend fun aging(farmId: String) = runCatching {
        db["buyer_outstanding_aging"].select { filter {OutstandingAgingDto::farmId eq farmId }}
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
        require(items.isNotEmpty()) { "Dispatch requires at least one line item" }; require(items.all { it.trays > 0 && it.eggs > 0 && it.rate >= 0 }) { "Dispatch quantities and rates are invalid" };
        val saved = db["dispatches"].insert(
            DispatchDto(
                farmId = h.farmId,
                buyerId = h.buyerId,
                flockId = h.flockId,
                date = h.date,
                transport = h.transport,
                loadingCharges = h.loadingCharges,
                otherCharges = h.otherCharges,
                invoiceReference = h.invoiceReference,
                vehicle = h.vehicle,
                driver = h.driver,
                remarks = h.remarks,
                allowOversell = h.allowOversell
            )
        ) { select() }.decodeSingle<DispatchDto>().d(); items.forEach {
        db["dispatch_items"].insert(
            DispatchItemDto(saved.id, it.eggGradeId, it.trays, it.eggs, it.rate)
        )
    }; saved
    }

    override suspend fun recordPayment(v: BuyerPayment) = runCatching {
        require(v.amount > 0) { "Payment must be greater than zero" }; db["buyer_payments"].insert(
        BuyerPaymentDto(v.farmId, v.buyerId, v.date, v.amount, v.reference, v.method, v.notes)
    ); Unit
    }
}
