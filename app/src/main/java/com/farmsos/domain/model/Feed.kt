package com.farmsos.domain.model

enum class FeedType { LAYER_FEED, PRE_LAYER, STARTER, GROWER, CUSTOM_FEED }
data class FeedItem(val id: String = "", val farmId: String, val name: String, val feedType: FeedType, val unit: String = "kg", val openingQuantityKg: Double = 0.0, val openingCostPerKg: Double? = null, val openingDate: String = "", val isActive: Boolean = true)
data class FeedPurchase(val id: String = "", val feedItemId: String, val farmId: String, val supplier: String, val quantityKg: Double, val unit: String, val pricePerKg: Double, val batch: String, val purchaseDate: String, val expiryDate: String? = null, val remarks: String = "")
data class FeedConsumption(val id: String = "", val feedItemId: String, val farmId: String, val flockId: String? = null, val quantityKg: Double, val consumedDate: String, val remarks: String = "")
data class FeedAdjustment(val id: String = "", val feedItemId: String, val farmId: String, val quantityKg: Double, val adjustmentDate: String, val reason: String, val allowNegativeStock: Boolean = false)
data class FeedStock(val feedItemId: String, val quantityKg: Double, val averageCostPerKg: Double?)
data class FeedCostMetrics(val costPerKg: Double?, val costPerBirdDay: Double?, val costPerEgg: Double?, val costPerTray: Double?)
object FeedCalculator {
    fun stock(item: FeedItem, purchases: List<FeedPurchase>, consumption: List<FeedConsumption>, adjustments: List<FeedAdjustment>): FeedStock {
        val quantity = item.openingQuantityKg + purchases.sumOf { it.quantityKg } + adjustments.sumOf { it.quantityKg } - consumption.sumOf { it.quantityKg }
        val availablePurchased = item.openingQuantityKg * (item.openingCostPerKg ?: 0.0) + purchases.sumOf { it.quantityKg * it.pricePerKg }
        val availableQtyForCost = item.openingQuantityKg + purchases.sumOf { it.quantityKg }
        return FeedStock(item.id, quantity, availableQtyForCost.takeIf { it > 0 }?.let { availablePurchased / it })
    }
    fun costs(costPerKg: Double?, consumedKg: Double, averageLiveBirds: Double, eggs: Double, traySize: Double): FeedCostMetrics {
        val totalCost = costPerKg?.times(consumedKg)
        return FeedCostMetrics(costPerKg, if (averageLiveBirds > 0) totalCost?.div(averageLiveBirds) else null, if (eggs > 0) totalCost?.div(eggs) else null, if (eggs > 0 && traySize > 0) totalCost?.times(traySize)?.div(eggs) else null)
    }
}
