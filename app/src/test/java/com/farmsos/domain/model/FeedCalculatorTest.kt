package com.farmsos.domain.model
import org.junit.Assert.*
import org.junit.Test
class FeedCalculatorTest {
 @Test fun stockIsDerivedFromLedgerMovements(){val item=FeedItem(id="i",farmId="f",name="Layer",feedType=FeedType.LAYER_FEED,openingQuantityKg=10.0,openingCostPerKg=20.0);val stock=FeedCalculator.stock(item,listOf(FeedPurchase(feedItemId="i",farmId="f",supplier="s",quantityKg=20.0,unit="kg",pricePerKg=25.0,batch="b",purchaseDate="2026-08-27")),listOf(FeedConsumption(feedItemId="i",farmId="f",quantityKg=8.0,consumedDate="2026-08-27")),listOf(FeedAdjustment(feedItemId="i",farmId="f",quantityKg=-2.0,adjustmentDate="2026-08-27",reason="spill")));assertEquals(20.0,stock.quantityKg,0.001);assertEquals(23.333,stock.averageCostPerKg!!,0.001)}
 @Test fun costsUseActualCost(){val costs=FeedCalculator.costs(25.0,10.0,100.0,200.0,30.0);assertEquals(2.5,costs.costPerBirdDay!!,0.001);assertEquals(1.25,costs.costPerEgg!!,0.001);assertEquals(37.5,costs.costPerTray!!,0.001)}
}
