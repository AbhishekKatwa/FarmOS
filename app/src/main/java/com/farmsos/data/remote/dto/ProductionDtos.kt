package com.farmsos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class EggGradeDto(val id: String? = null, val code: String, @SerialName("display_name") val displayName: String, @SerialName("is_active") val isActive: Boolean = true, @SerialName("sort_order") val sortOrder: Int = 0)
@Serializable data class DailyProductionDto(
    val id: String? = null, @SerialName("farm_id") val farmId: String, @SerialName("shed_id") val shedId: String,
    @SerialName("flock_id") val flockId: String, val date: String, @SerialName("opening_live_birds") val openingLiveBirds: Int,
    val mortality: Int = 0, val culls: Int = 0, @SerialName("closing_live_birds") val closingLiveBirds: Int? = null,
    @SerialName("eggs_collected") val eggsCollected: Int = 0, @SerialName("broken_eggs") val brokenEggs: Int = 0,
    @SerialName("dirty_eggs") val dirtyEggs: Int = 0, @SerialName("usable_eggs") val usableEggs: Int = 0,
    @SerialName("rejected_eggs") val rejectedEggs: Int = 0, @SerialName("feed_consumed_kg") val feedConsumedKg: Double = 0.0,
    val remarks: String = "", @SerialName("entered_by") val enteredBy: String? = null
)
@Serializable data class DailyProductionWriteDto(
    @SerialName("farm_id") val farmId: String, @SerialName("shed_id") val shedId: String,
    @SerialName("flock_id") val flockId: String, val date: String, @SerialName("opening_live_birds") val openingLiveBirds: Int,
    val mortality: Int, val culls: Int, @SerialName("eggs_collected") val eggsCollected: Int,
    @SerialName("broken_eggs") val brokenEggs: Int, @SerialName("dirty_eggs") val dirtyEggs: Int,
    @SerialName("usable_eggs") val usableEggs: Int, @SerialName("rejected_eggs") val rejectedEggs: Int,
    @SerialName("feed_consumed_kg") val feedConsumedKg: Double, val remarks: String
)
@Serializable data class EggGradeEntryDto(@SerialName("production_daily_id") val productionDailyId: String, @SerialName("egg_grade_id") val eggGradeId: String, val quantity: Int)
@Serializable data class MortalityRecordDto(val id: String? = null, @SerialName("production_daily_id") val productionDailyId: String, @SerialName("farm_id") val farmId: String, @SerialName("flock_id") val flockId: String, val date: String, @SerialName("mortality_count") val mortalityCount: Int, val cause: String = "", val remarks: String = "")
