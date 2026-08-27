package com.farmsos.domain.usecase.farm

import com.farmsos.domain.model.DailyProduction
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionValidationTest {
    private fun record(opening: Int = 10, mortality: Int = 0, culls: Int = 0, eggs: Int = 0, feed: Double = 0.0) = DailyProduction(
        localId = "local", farmId = "farm", shedId = "shed", flockId = "flock", date = "2026-08-27", openingLiveBirds = opening, mortality = mortality, culls = culls,
        closingLiveBirds = opening - mortality - culls, eggsCollected = eggs, brokenEggs = 0, dirtyEggs = 0, usableEggs = 0, rejectedEggs = 0, feedConsumedKg = feed,
        idempotencyKey = "key", createdAt = 0, updatedAt = 0
    )
    @Test fun rejectsNegativeEggsFeedAndMortality() {
        assertTrue(validateProduction(record(eggs = -1))!!.isFailure)
        assertTrue(validateProduction(record(feed = -0.1))!!.isFailure)
        assertTrue(validateProduction(record(mortality = -1))!!.isFailure)
    }
    @Test fun rejectsMortalityAndCullsAboveAvailableBirds() { assertTrue(validateProduction(record(opening = 10, mortality = 8, culls = 3))!!.isFailure) }
}
