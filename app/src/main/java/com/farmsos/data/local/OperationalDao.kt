package com.farmsos.data.local

import androidx.room.*
import com.farmsos.domain.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationalDao {
    // Production
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduction(production: DailyProduction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEggGradeEntries(entries: List<EggGradeEntry>)

    @Query("DELETE FROM egg_grade_entries WHERE productionDailyLocalId = :productionLocalId")
    suspend fun deleteEggGradeEntries(productionLocalId: String)

    @Query("SELECT * FROM production_daily WHERE flockId = :flockId ORDER BY date DESC")
    fun getProductionByFlock(flockId: String): Flow<List<DailyProduction>>

    @Query("SELECT * FROM egg_grade_entries WHERE productionDailyLocalId = :productionLocalId")
    suspend fun getEggGradeEntries(productionLocalId: String): List<EggGradeEntry>

    // Mortality
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMortality(record: MortalityRecord)

    @Query("SELECT * FROM mortality_records WHERE productionDailyId = :productionDailyId")
    suspend fun getMortalityByProduction(productionDailyId: String): MortalityRecord?

    // Feed
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedPurchase(purchase: FeedPurchase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedConsumption(consumption: FeedConsumption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedAdjustment(adjustment: FeedAdjustment)

    // Sales
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatch(dispatch: Dispatch)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatchItems(items: List<DispatchItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuyerPayment(payment: BuyerPayment)

    // Sync Queries
    @Query("SELECT * FROM production_daily WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingProduction(): List<DailyProduction>

    @Query("SELECT * FROM mortality_records WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingMortality(): List<MortalityRecord>

    @Query("SELECT * FROM feed_purchases WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingFeedPurchases(): List<FeedPurchase>

    @Query("SELECT * FROM feed_consumption WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingFeedConsumption(): List<FeedConsumption>

    @Query("SELECT * FROM dispatches WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingDispatches(): List<Dispatch>

    @Query("SELECT * FROM buyer_payments WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingBuyerPayments(): List<BuyerPayment>

    @Update
    suspend fun updateProduction(production: DailyProduction)

    @Update
    suspend fun updateMortality(record: MortalityRecord)

    @Update
    suspend fun updateFeedPurchase(purchase: FeedPurchase)

    @Update
    suspend fun updateFeedConsumption(consumption: FeedConsumption)

    @Update
    suspend fun updateDispatch(dispatch: Dispatch)

    @Update
    suspend fun updateBuyerPayment(payment: BuyerPayment)
}
