package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: String,
    val category: Category,
    val monthlyLimit: Double,
    val currentSpend: Double = 0.0,
    val alertThresholdPercent: Double = 80.0
) {
    val remainingBudget: Double get() = (monthlyLimit - currentSpend).coerceAtLeast(0.0)
    val spendPercentage: Double get() = if (monthlyLimit > 0) (currentSpend / monthlyLimit) * 100.0 else 0.0
    val isNearLimit: Boolean get() = spendPercentage >= alertThresholdPercent
    val isOverBudget: Boolean get() = currentSpend > monthlyLimit
}
