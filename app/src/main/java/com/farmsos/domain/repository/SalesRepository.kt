package com.farmsos.domain.repository
import com.farmsos.domain.model.*
interface SalesRepository { suspend fun buyers(farmId:String):Result<List<Buyer>>; suspend fun dispatches(buyerId:String):Result<List<Dispatch>>; suspend fun balances(farmId:String):Result<List<BuyerBalance>>; suspend fun aging(farmId:String):Result<List<OutstandingAging>>; suspend fun addBuyer(value:Buyer):Result<Buyer>; suspend fun createDispatch(header:Dispatch,items:List<DispatchItem>):Result<Dispatch>; suspend fun recordPayment(value:BuyerPayment):Result<Unit> }
