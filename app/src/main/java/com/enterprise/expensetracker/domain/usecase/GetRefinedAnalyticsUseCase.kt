package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ReportPeriod
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

data class ItemConsumptionReport(
    val itemName: String,
    val categoryName: String,
    val currentSpend: Double,
    val currentQuantity: Double,
    val unit: String,
    val purchaseCount: Int,
    val avgPurchasePrice: Double,
    val avgUsageDays: Double,
    val restockCadenceDays: Double,
    val prevSpend: Double,
    val prevQuantity: Double,
    val costChangePercent: Double,
    val quantityChangePercent: Double,
    val unitPriceChangePercent: Double
)

data class RefinedAnalyticsReport(
    val selectedPeriod: ReportPeriod,
    val periodLabel: String,
    val totalSpendCurrent: Double,
    val totalSpendPrevious: Double,
    val totalCostChangePercent: Double,
    val totalItemsPurchased: Int,
    val categoryBreakdown: Map<String, Double>,
    val itemReports: List<ItemConsumptionReport>,
    val topConsumedItems: List<ItemConsumptionReport>,
    val highPriceInflationItems: List<ItemConsumptionReport>
)

class GetRefinedAnalyticsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(period: ReportPeriod): Flow<RefinedAnalyticsReport> {
        return expenseRepository.observeAllExpenses().map { allExpenses ->
            calculateReport(allExpenses, period)
        }
    }

    private fun calculateReport(allExpenses: List<Expense>, period: ReportPeriod): RefinedAnalyticsReport {
        val now = System.currentTimeMillis()
        val (currentStart, prevStart, prevEnd, label) = getPeriodBoundaries(now, period)

        val currentExpenses = allExpenses.filter { it.timestamp in currentStart..now }
        val prevExpenses = allExpenses.filter { it.timestamp in prevStart until prevEnd }

        val currentTotalSpend = currentExpenses.sumOf { it.amount }
        val prevTotalSpend = prevExpenses.sumOf { it.amount }
        val totalCostChangePercent = calculatePercentageChange(prevTotalSpend, currentTotalSpend)

        // Category breakdown
        val categoryBreakdown = currentExpenses
            .groupBy { it.category.displayName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Group by item name lowercase
        val allItemKeys = (currentExpenses.map { it.title.trim().lowercase() } + prevExpenses.map { it.title.trim().lowercase() }).distinct()

        val itemReports = allItemKeys.mapNotNull { itemKey ->
            val currItems = currentExpenses.filter { it.title.trim().equals(itemKey, ignoreCase = true) }
            val prevItems = prevExpenses.filter { it.title.trim().equals(itemKey, ignoreCase = true) }

            if (currItems.isEmpty() && prevItems.isEmpty()) return@mapNotNull null

            val displayName = currItems.firstOrNull()?.title ?: prevItems.firstOrNull()?.title ?: itemKey.replaceFirstChar { it.uppercase() }
            val category = (currItems.firstOrNull() ?: prevItems.firstOrNull())?.category?.displayName ?: "Groceries"
            val unit = (currItems.firstOrNull() ?: prevItems.firstOrNull())?.unit ?: "item"

            val currSpend = currItems.sumOf { it.amount }
            val currQty = currItems.sumOf { it.quantity }.coerceAtLeast(0.0)
            val prevSpend = prevItems.sumOf { it.amount }
            val prevQty = prevItems.sumOf { it.quantity }.coerceAtLeast(0.0)

            val purchaseCount = currItems.size
            val avgPrice = if (currQty > 0) currSpend / currQty else if (purchaseCount > 0) currSpend / purchaseCount else 0.0
            val prevAvgPrice = if (prevQty > 0) prevSpend / prevQty else if (prevItems.isNotEmpty()) prevSpend / prevItems.size else 0.0

            val costChange = calculatePercentageChange(prevSpend, currSpend)
            val qtyChange = calculatePercentageChange(prevQty, currQty)
            val unitPriceChange = calculatePercentageChange(prevAvgPrice, avgPrice)

            // Calculate restock cadence & usage duration
            val allHistoryForItem = allExpenses
                .filter { it.title.trim().equals(itemKey, ignoreCase = true) }
                .sortedBy { it.timestamp }

            var avgCadence = 6.0
            if (allHistoryForItem.size >= 2) {
                val diffs = (0 until allHistoryForItem.size - 1).map { i ->
                    (allHistoryForItem[i + 1].timestamp - allHistoryForItem[i].timestamp).toDouble() / (1000 * 60 * 60 * 24)
                }
                avgCadence = (diffs.average()).coerceAtLeast(1.0)
            }

            val avgUsageDays = if (currQty > 0 && avgCadence > 0) {
                (avgCadence / currQty).coerceIn(0.5, 30.0)
            } else 4.0

            ItemConsumptionReport(
                itemName = displayName,
                categoryName = category,
                currentSpend = currSpend,
                currentQuantity = currQty,
                unit = unit,
                purchaseCount = purchaseCount,
                avgPurchasePrice = avgPrice,
                avgUsageDays = (avgUsageDays * 10).toInt() / 10.0,
                restockCadenceDays = (avgCadence * 10).toInt() / 10.0,
                prevSpend = prevSpend,
                prevQuantity = prevQty,
                costChangePercent = (costChange * 10).toInt() / 10.0,
                quantityChangePercent = (qtyChange * 10).toInt() / 10.0,
                unitPriceChangePercent = (unitPriceChange * 10).toInt() / 10.0
            )
        }.sortedByDescending { it.currentSpend }

        val topConsumed = itemReports.sortedByDescending { it.currentQuantity }.take(5)
        val highInflation = itemReports.filter { it.unitPriceChangePercent > 0 }.sortedByDescending { it.unitPriceChangePercent }.take(5)

        return RefinedAnalyticsReport(
            selectedPeriod = period,
            periodLabel = label,
            totalSpendCurrent = currentTotalSpend,
            totalSpendPrevious = prevTotalSpend,
            totalCostChangePercent = (totalCostChangePercent * 10).toInt() / 10.0,
            totalItemsPurchased = currentExpenses.size,
            categoryBreakdown = categoryBreakdown,
            itemReports = itemReports,
            topConsumedItems = topConsumed,
            highPriceInflationItems = highInflation
        )
    }

    private fun calculatePercentageChange(oldVal: Double, newVal: Double): Double {
        if (oldVal <= 0.0) return if (newVal > 0.0) 100.0 else 0.0
        return ((newVal - oldVal) / oldVal) * 100.0
    }

    private data class Boundaries(val currentStart: Long, val prevStart: Long, val prevEnd: Long, val label: String)

    private fun getPeriodBoundaries(now: Long, period: ReportPeriod): Boundaries {
        val cal = Calendar.getInstance().apply { timeInMillis = now }

        return when (period) {
            ReportPeriod.DAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val currentStart = cal.timeInMillis

                val oneDay = 24 * 60 * 60 * 1000L
                val prevStart = currentStart - oneDay
                val prevEnd = currentStart
                Boundaries(currentStart, prevStart, prevEnd, "Today vs Yesterday")
            }
            ReportPeriod.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val currentStart = cal.timeInMillis

                val oneWeek = 7 * 24 * 60 * 60 * 1000L
                val prevStart = currentStart - oneWeek
                val prevEnd = currentStart
                Boundaries(currentStart, prevStart, prevEnd, "This Week vs Last Week")
            }
            ReportPeriod.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val currentStart = cal.timeInMillis

                cal.add(Calendar.MONTH, -1)
                val prevStart = cal.timeInMillis
                Boundaries(currentStart, prevStart, currentStart, "This Month vs Last Month")
            }
            ReportPeriod.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val currentStart = cal.timeInMillis

                cal.add(Calendar.YEAR, -1)
                val prevStart = cal.timeInMillis
                Boundaries(currentStart, prevStart, currentStart, "This Year vs Last Year")
            }
        }
    }
}
