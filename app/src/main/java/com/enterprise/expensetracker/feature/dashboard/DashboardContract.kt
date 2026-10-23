package com.enterprise.expensetracker.feature.dashboard

import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ai.AiInsight

data class DashboardState(
    val isLoading: Boolean = false,
    val totalSpendThisMonth: Double = 0.0,
    val monthlyBudget: Double = 45000.0,
    val predictedEndOfMonthSpend: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(),
    val featuredInsight: AiInsight? = null,
    val activeAiEngine: String = "AI Advisor",
    val currencySymbol: String = "₹"
)

sealed interface DashboardEvent {
    data object RefreshData : DashboardEvent
    data object SyncData : DashboardEvent
    data object AddDemoExpense : DashboardEvent
    data class DeleteExpense(val id: String) : DashboardEvent
}

sealed interface DashboardEffect {
    data class ShowToast(val message: String) : DashboardEffect
}
