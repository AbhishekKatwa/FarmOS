package com.farmsos.domain.model
data class DashboardDaily(val farmId:String,val shedId:String?,val flockId:String?,val date:String,val liveBirds:Double,val eggs:Double,val mortality:Double,val feedKg:Double,val henDay:Double?)
data class DashboardSale(val farmId:String,val flockId:String?,val date:String,val eggs:Double,val trays:Double,val revenue:Double,val eggRate:Double?)
data class DashboardFinance(val farmId:String,val flockId:String?,val date:String,val revenue:Double,val expenses:Double,val profit:Double)
data class DashboardGrade(val farmId:String,val shedId:String?,val flockId:String?,val date:String,val grade:String,val eggs:Double)
data class DashboardAlert(val farmId:String,val type:String,val message:String,val dueDate:String)
data class DashboardSnapshot(val liveBirds:Double=0.0,val eggsToday:Double=0.0,val henDay:Double?=null,val mortality:Double=0.0,val feedUsedKg:Double=0.0,val feedCost:Double=0.0,val currentEggRate:Double?=null,val revenue:Double=0.0,val expenses:Double=0.0,val profit:Double=0.0,val buyerOutstanding:Double=0.0,val feedStockDays:Double?=null,val alerts:List<DashboardAlert> = emptyList(),val production:List<DashboardDaily> = emptyList(),val sales:List<DashboardSale> = emptyList(),val finance:List<DashboardFinance> = emptyList(),val grades:List<DashboardGrade> = emptyList())
