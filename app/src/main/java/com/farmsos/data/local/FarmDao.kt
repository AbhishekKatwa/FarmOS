package com.farmsos.data.local

import androidx.room.*
import com.farmsos.domain.model.Farm
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE id = :id")
    suspend fun getFarmById(id: String): Farm?

    @Query("SELECT * FROM farms WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    suspend fun getFarmsByOwner(ownerId: String): List<Farm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: Farm)

    @Update
    suspend fun updateFarm(farm: Farm)

    @Delete
    suspend fun deleteFarm(farm: Farm)

    @Query("SELECT * FROM farms ORDER BY createdAt DESC")
    fun getAllFarms(): Flow<List<Farm>>
}
