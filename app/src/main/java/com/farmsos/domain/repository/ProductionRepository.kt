package com.farmsos.domain.repository

import com.farmsos.domain.model.DailyProduction
import com.farmsos.domain.model.EggGrade

interface ProductionRepository {
    suspend fun listGrades(): Result<List<EggGrade>>
    suspend fun listByFlock(flockId: String): Result<List<DailyProduction>>
    suspend fun get(id: String): Result<DailyProduction>
    suspend fun create(production: DailyProduction): Result<DailyProduction>
    suspend fun update(production: DailyProduction): Result<DailyProduction>
}
