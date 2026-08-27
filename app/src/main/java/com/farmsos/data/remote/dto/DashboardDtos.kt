package com.farmsos.data.remote.dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable data class DashboardDailyDto(@SerialName("farm_id")val farmId:String,@SerialName("shed_id")val shedId:String?=null,@SerialName("flock_id")val flockId:String?=null,val date:String,@SerialName("live_birds")val liveBirds:Double,@SerialName("eggs_collected")val eggs:Double,val mortality:Double,@SerialName("feed_used_kg")val feedKg:Double,@SerialName("hen_day_percent")val henDay:Double?=null)
@Serializable data class DashboardSaleDto(@SerialName("farm_id")val farmId:String,@SerialName("flock_id")val flockId:String?=null,val date:String,val eggs:Double,val trays:Double,val revenue:Double,@SerialName("egg_rate")val eggRate:Double?=null)
@Serializable data class DashboardFinanceDto(@SerialName("farm_id")val farmId:String,@SerialName("flock_id")val flockId:String?=null,val date:String,val revenue:Double=0.0,val expenses:Double=0.0,val profit:Double=0.0)
@Serializable data class DashboardGradeDto(@SerialName("farm_id")val farmId:String,@SerialName("shed_id")val shedId:String?=null,@SerialName("flock_id")val flockId:String?=null,val date:String,val grade:String,val eggs:Double)
@Serializable data class DashboardFeedDto(@SerialName("farm_id")val farmId:String,@SerialName("stock_days")val stockDays:Double?=null)
@Serializable data class DashboardAlertDto(@SerialName("farm_id")val farmId:String,@SerialName("alert_type")val type:String,val message:String,@SerialName("due_date")val dueDate:String)
