package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class BudgetScope {
    FAMILY,
    INDIVIDUAL
}

@Serializable
enum class BudgetPeriod {
    WEEKLY,
    MONTHLY
}

@Serializable
enum class ReportPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

@Serializable
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val familyId: String = "family_primary",
    val userId: String? = null,
    val userName: String? = null,
    val scope: BudgetScope = BudgetScope.FAMILY,
    val baseAmount: Double = 50000.0,
    val periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val weeklyAmount: Double
        get() = when (periodType) {
            BudgetPeriod.WEEKLY -> baseAmount
            BudgetPeriod.MONTHLY -> baseAmount / 4.333
        }

    val monthlyAmount: Double
        get() = when (periodType) {
            BudgetPeriod.MONTHLY -> baseAmount
            BudgetPeriod.WEEKLY -> baseAmount * 4.333
        }

    val yearlyAmount: Double
        get() = when (periodType) {
            BudgetPeriod.MONTHLY -> baseAmount * 12.0
            BudgetPeriod.WEEKLY -> baseAmount * 52.0
        }

    fun getAmountForReportPeriod(period: ReportPeriod): Double {
        return when (period) {
            ReportPeriod.DAY -> weeklyAmount / 7.0
            ReportPeriod.WEEK -> weeklyAmount
            ReportPeriod.MONTH -> monthlyAmount
            ReportPeriod.YEAR -> yearlyAmount
        }
    }
}
