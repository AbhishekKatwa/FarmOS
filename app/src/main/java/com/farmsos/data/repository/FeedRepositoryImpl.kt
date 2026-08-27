package com.farmsos.data.repository
import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.FeedRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
@Singleton class FeedRepositoryImpl @Inject constructor(private val db:Postgrest):FeedRepository {
 private fun FeedItemDto.domain()=FeedItem(id.orEmpty(),farmId,name,FeedType.valueOf(feedType),unit,openingQuantityKg,openingCostPerKg,openingDate.take(10),isActive)
 private fun FeedPurchaseDto.domain()=FeedPurchase(id.orEmpty(),feedItemId,farmId,supplier,quantityKg,unit,pricePerKg,batch,purchaseDate.take(10),expiryDate?.take(10),remarks)
 private fun FeedConsumptionDto.domain()=FeedConsumption(id.orEmpty(),feedItemId,farmId,flockId,quantityKg,consumedDate.take(10),remarks)
 private fun FeedAdjustmentDto.domain()=FeedAdjustment(id.orEmpty(),feedItemId,farmId,quantityKg,adjustmentDate.take(10),reason,allowNegativeStock)
 override suspend fun items(farmId:String)=runCatching{db["feed_items"].select{FeedItemDto::farmId eq farmId}.decodeList<FeedItemDto>().map{it.domain()}}
 override suspend fun purchases(itemId:String)=runCatching{db["feed_purchases"].select{FeedPurchaseDto::feedItemId eq itemId}.decodeList<FeedPurchaseDto>().map{it.domain()}}
 override suspend fun consumption(itemId:String)=runCatching{db["feed_consumption"].select{FeedConsumptionDto::feedItemId eq itemId}.decodeList<FeedConsumptionDto>().map{it.domain()}}
 override suspend fun adjustments(itemId:String)=runCatching{db["feed_adjustments"].select{FeedAdjustmentDto::feedItemId eq itemId}.decodeList<FeedAdjustmentDto>().map{it.domain()}}
 override suspend fun addItem(item:FeedItem)=runCatching{db["feed_items"].insert(FeedItemDto(farmId=item.farmId,name=item.name,feedType=item.feedType.name,unit=item.unit,openingQuantityKg=item.openingQuantityKg,openingCostPerKg=item.openingCostPerKg,openingDate=item.openingDate)){select()}.decodeSingle<FeedItemDto>().domain()}
 override suspend fun addPurchase(value:FeedPurchase)=runCatching{db["feed_purchases"].insert(FeedPurchaseDto(feedItemId=value.feedItemId,farmId=value.farmId,supplier=value.supplier,quantityKg=value.quantityKg,unit=value.unit,pricePerKg=value.pricePerKg,batch=value.batch,purchaseDate=value.purchaseDate,expiryDate=value.expiryDate,remarks=value.remarks));Unit}
 override suspend fun addConsumption(value:FeedConsumption)=runCatching{db["feed_consumption"].insert(FeedConsumptionDto(feedItemId=value.feedItemId,farmId=value.farmId,flockId=value.flockId,quantityKg=value.quantityKg,consumedDate=value.consumedDate,remarks=value.remarks));Unit}
 override suspend fun addAdjustment(value:FeedAdjustment)=runCatching{db["feed_adjustments"].insert(FeedAdjustmentDto(feedItemId=value.feedItemId,farmId=value.farmId,quantityKg=value.quantityKg,adjustmentDate=value.adjustmentDate,reason=value.reason,allowNegativeStock=value.allowNegativeStock));Unit}
}
