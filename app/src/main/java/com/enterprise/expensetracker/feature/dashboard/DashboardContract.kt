package com.enterprise.expensetracker.feature.dashboard

import com.enterprise.expensetracker.core.model.Budget
import com.enterprise.expensetracker.core.model.BudgetPeriod
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.ai.AiInsight

enum class BudgetPeriodView {
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class DashboardState(
    val isLoading: Boolean = false,
    val currentUser: UserProfile? = null,
    val selectedPeriodView: BudgetPeriodView = BudgetPeriodView.MONTHLY,
    // Family Head Budget & Spend
    val familyBudget: Budget? = null,
    val familySpendInPeriod: Double = 0.0,
    // Member Personal Budget & Spend
    val memberBudget: Budget? = null,
    val memberSpendInPeriod: Double = 0.0,
    // Display properties
    val predictedEndOfMonthSpend: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(),
    val featuredInsight: AiInsight? = null,
    val activeAiEngine: String = "AI Advisor",
    val currencySymbol: String = "₹",
    val isBudgetDialogOpen: Boolean = false
) {
    val isHeadOfHousehold: Boolean
        get() = currentUser?.role?.name == "HEAD_OF_HOUSEHOLD" || currentUser?.relationship?.name == "HEAD"

    // Active Family Budget converted to selected view
    val effectiveFamilyBudgetAmount: Double
        get() {
            val b = familyBudget ?: return 50000.0
            return when (selectedPeriodView) {
                BudgetPeriodView.WEEKLY -> b.weeklyAmount
                BudgetPeriodView.MONTHLY -> b.monthlyAmount
                BudgetPeriodView.YEARLY -> b.yearlyAmount
            }
        }

    val familyRemainingBalance: Double
        get() = effectiveFamilyBudgetAmount - familySpendInPeriod

    val familyBudgetPercent: Double
        get() = if (effectiveFamilyBudgetAmount > 0) (familySpendInPeriod / effectiveFamilyBudgetAmount) * 100.0 else 0.0

    // Member Budget converted to selected view
    val effectiveMemberBudgetAmount: Double?
        get() {
            val b = memberBudget ?: return null
            return when (selectedPeriodView) {
                BudgetPeriodView.WEEKLY -> b.weeklyAmount
                BudgetPeriodView.MONTHLY -> b.monthlyAmount
                BudgetPeriodView.YEARLY -> b.yearlyAmount
            }
        }

    val memberRemainingBalance: Double?
        get() = effectiveMemberBudgetAmount?.let { it - memberSpendInPeriod }

    val memberBudgetPercent: Double?
        get() = effectiveMemberBudgetAmount?.let { if (it > 0) (memberSpendInPeriod / it) * 100.0 else 0.0 }
}

sealed interface DashboardEvent {
    data object RefreshData : DashboardEvent
    data object SyncData : DashboardEvent
    data class SelectPeriodView(val period: BudgetPeriodView) : DashboardEvent
    data class SetBudgetDialogVisible(val visible: Boolean) : DashboardEvent
    data class SaveBudgetSubmitted(
        val amount: Double,
        val periodType: BudgetPeriod,
        val isFamilyBudget: Boolean
    ) : DashboardEvent
    data class DeleteExpense(val id: String) : DashboardEvent
}

sealed interface DashboardEffect {
    data class ShowToast(val message: String) : DashboardEffect
}
