package com.farmsos.domain.model
data class Buyer(val id:String="",val farmId:String,val name:String,val code:String,val phone:String="",val address:String="",val location:String="",val paymentTerms:String="",val creditLimit:Double=0.0,val openingBalance:Double=0.0,val active:Boolean=true,val notes:String="")
data class Dispatch(val id:String="",val farmId:String,val buyerId:String,val flockId:String?=null,val date:String,val transport:Double=0.0,val loadingCharges:Double=0.0,val otherCharges:Double=0.0,val invoiceReference:String="",val vehicle:String="",val driver:String="",val remarks:String="",val allowOversell:Boolean=false)
data class DispatchItem(val dispatchId:String,val eggGradeId:String,val trays:Double,val eggs:Int,val rate:Double)
data class BuyerPayment(val farmId:String,val buyerId:String,val date:String,val amount:Double,val reference:String="",val method:String="",val notes:String="")
data class BuyerBalance(val buyerId:String,val farmId:String,val name:String,val closingBalance:Double)
data class OutstandingAging(val buyerId:String,val farmId:String,val current:Double,val days1To7:Double,val days8To30:Double,val days31To60:Double,val days60Plus:Double)
