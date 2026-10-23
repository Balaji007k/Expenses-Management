package com.enterprise.expensetracker.core.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class CategoryForecast(
    val categoryName: String,
    val historicalAverage: Double,
    val predictedAmount: Double,
    val trendPercentage: Double
)

@Serializable
data class SpendingForecast(
    val currentMonthActual: Double,
    val predictedEndOfMonthSpend: Double,
    val historicalMonthlyAverage: Double,
    val confidenceRate: Float,
    val predictedBurnRatePerDay: Double,
    val daysRemainingInPeriod: Int,
    val breakdownByCategory: List<CategoryForecast> = emptyList(),
    val summaryNarrative: String
)
