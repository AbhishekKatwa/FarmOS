package com.farmsos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FlockAgeCalculatorTest {

    @Test
    fun rejectsInvalidDates() {
        assertFalse(FlockAgeCalculator.isValidPlacementDate(""))
        assertFalse(FlockAgeCalculator.isValidPlacementDate("2026/01/01"))
        assertFalse(FlockAgeCalculator.isValidPlacementDate("2026-13-40"))
        assertTrue(FlockAgeCalculator.isValidPlacementDate("2026-01-15"))
    }

    @Test
    fun calculatesDaysWeeksAndProductionWeek() {
        val placement = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 15, 12, 0, 0)
        }
        val age = FlockAgeCalculator.calculate("2026-01-01", now.timeInMillis)
        assertEquals(14, age.days)
        assertEquals(2, age.weeks)
        assertEquals(3, age.productionWeek)
        assertTrue(placement.timeInMillis < now.timeInMillis)
    }
}
