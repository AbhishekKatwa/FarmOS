package com.farmsos.domain.model

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Bird age is derived from placement_date and is never persisted.
 */
object FlockAgeCalculator {
    private val isoDate = Regex("""^\d{4}-\d{2}-\d{2}$""")

    fun isValidPlacementDate(value: String): Boolean {
        if (!isoDate.matches(value.trim())) return false
        return parseDate(value.trim()) != null
    }

    fun calculate(placementDate: String, nowMillis: Long = System.currentTimeMillis()): FlockAge {
        val placement = parseDate(placementDate.trim())
            ?: return FlockAge(days = 0, weeks = 0, productionWeek = 0)
        val today = startOfDay(nowMillis)
        val days = TimeUnit.MILLISECONDS.toDays(today.timeInMillis - placement.timeInMillis).toInt()
        val safeDays = days.coerceAtLeast(0)
        val weeks = safeDays / 7
        val productionWeek = if (safeDays == 0) 1 else weeks + 1
        return FlockAge(days = safeDays, weeks = weeks, productionWeek = productionWeek)
    }

    private fun parseDate(value: String): Calendar? {
        val parts = value.split("-")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null
        if (month !in 1..12 || day !in 1..31) return null
        return Calendar.getInstance().apply {
            isLenient = false
            try {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                timeInMillis
            } catch (_: Exception) {
                return null
            }
        }
    }

    private fun startOfDay(millis: Long): Calendar =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
}
