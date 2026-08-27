package com.farmsos.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class Buyer(val id:String="",val farmId:String,val name:String,val code:String,val phone:String="",val address:String="",val location:String="",val paymentTerms:String="",val creditLimit:Double=0.0,val openingBalance:Double=0.0,val active:Boolean=true,val notes:String="")

@Entity(tableName = "dispatches")
data class Dispatch(
    @PrimaryKey override val localId: String = "",
    val id: String = "",
    val farmId: String,
    val buyerId: String,
    val flockId: String? = null,
    val date: String,
    val transport: Double = 0.0,
    val loadingCharges: Double = 0.0,
    val otherCharges: Double = 0.0,
    val invoiceReference: String = "",
    val vehicle: String = "",
    val driver: String = "",
    val remarks: String = "",
    val allowOversell: Boolean = false,
    override val serverId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.PENDING,
    override val syncAttempts: Int = 0,
    override val lastSyncError: String? = null,
    override val idempotencyKey: String = "",
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : Syncable

@Entity(tableName = "dispatch_items")
data class DispatchItem(
    @PrimaryKey(autoGenerate = true) val localEntryId: Int = 0,
    val dispatchLocalId: String,
    val eggGradeId: String,
    val trays: Double,
    val eggs: Int,
    val rate: Double
)

@Entity(tableName = "buyer_payments")
data class BuyerPayment(
    @PrimaryKey override val localId: String = "",
    val farmId: String,
    val buyerId: String,
    val date: String,
    val amount: Double,
    val reference: String = "",
    val method: String = "",
    val notes: String = "",
    override val serverId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.PENDING,
    override val syncAttempts: Int = 0,
    override val lastSyncError: String? = null,
    override val idempotencyKey: String = "",
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0
) : Syncable
data class BuyerBalance(val buyerId:String,val farmId:String,val name:String,val closingBalance:Double)
data class OutstandingAging(val buyerId:String,val farmId:String,val current:Double,val days1To7:Double,val days8To30:Double,val days31To60:Double,val days60Plus:Double)
