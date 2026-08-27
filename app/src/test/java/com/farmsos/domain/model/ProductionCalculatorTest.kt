package com.farmsos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductionCalculatorTest {
    private fun production(opening: Int = 100, mortality: Int = 4, culls: Int = 6, eggs: Int = 90, feed: Double = 12.0) = DailyProduction(
        farmId = "farm", shedId = "shed", flockId = "flock", date = "2026-08-27", openingLiveBirds = opening,
        mortality = mortality, culls = culls, eggsCollected = eggs, brokenEggs = 0, dirtyEggs = 0, usableEggs = eggs, rejectedEggs = 0, feedConsumedKg = feed
    )

    @Test fun calculatesClosingAndMetricsFromAverageLiveBirds() {
        val result = ProductionCalculator.metrics(production())
        assertEquals(90, ProductionCalculator.closingLiveBirds(100, 4, 6))
        assertEquals(95.0, result.averageLiveBirds, 0.001)
        assertEquals(94.7368, result.henDayPercent!!, 0.001)
        assertEquals(0.947368, result.eggsPerBird!!, 0.001)
        assertEquals(0.126316, result.feedPerBirdKg!!, 0.001)
        assertEquals(0.133333, result.feedPerEggKg!!, 0.001)
    }

    @Test fun avoidsDivisionByZero() {
        val result = ProductionCalculator.metrics(production(opening = 0, mortality = 0, culls = 0, eggs = 0, feed = 0.0))
        assertNull(result.henDayPercent); assertNull(result.eggsPerBird); assertNull(result.feedPerBirdKg); assertNull(result.feedPerEggKg)
    }
}
