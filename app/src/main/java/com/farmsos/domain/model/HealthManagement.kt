package com.farmsos.domain.model
data class Medicine(val id:String="",val farmId:String,val name:String,val manufacturer:String="",val category:String="",val unit:String,val openingStock:Double=0.0,val openingCost:Double?=null,val notes:String="")
data class MedicinePurchase(val medicineId:String,val farmId:String,val supplier:String,val quantity:Double,val unit:String,val purchaseCost:Double,val batch:String,val purchaseDate:String,val expiryDate:String?=null,val notes:String="")
data class MedicineUsage(val medicineId:String,val farmId:String,val shedId:String?=null,val flockId:String?=null,val date:String,val quantity:Double,val reason:String,val notes:String="")
data class Vaccine(val id:String="",val farmId:String,val name:String,val manufacturer:String="",val notes:String="")
data class VaccinationSchedule(val id:String="",val farmId:String,val vaccineId:String,val targetFlockId:String?=null,val plannedDate:String,val dose:String="",val route:String="",val notes:String="")
data class VaccinationRecord(val scheduleId:String?=null,val vaccineId:String,val farmId:String,val targetFlockId:String?=null,val plannedDate:String?=null,val actualDate:String,val dose:String="",val route:String="",val notes:String="")
data class VaccinationReminder(val scheduleId:String,val farmId:String,val targetFlockId:String?,val plannedDate:String,val vaccineName:String,val dose:String,val route:String)
data class MedicineExpiryAlert(val purchaseId:String,val farmId:String,val medicineId:String,val name:String,val batch:String,val expiryDate:String,val quantity:Double,val unit:String)
