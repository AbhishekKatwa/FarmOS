package com.farmsos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.farmsos.domain.model.*

@Database(
    entities = [
        Farm::class,
        DailyProduction::class,
        EggGradeEntry::class,
        MortalityRecord::class,
        FeedPurchase::class,
        FeedConsumption::class,
        FeedAdjustment::class,
        Dispatch::class,
        DispatchItem::class,
        BuyerPayment::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun operationalDao(): OperationalDao
}
