package com.farmsos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.farmsos.domain.model.Farm

@Database(
    entities = [Farm::class],
    version = 1,
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
}
