package com.farmsos.domain.repository
import com.farmsos.domain.model.DashboardSnapshot
interface DashboardRepository { suspend fun load(farmId:String?,shedId:String?,flockId:String?,start:String,end:String):Result<DashboardSnapshot> }
