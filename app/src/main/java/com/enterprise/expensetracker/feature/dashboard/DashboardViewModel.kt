package com.enterprise.expensetracker.feature.dashboard

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.BudgetPeriod
import com.enterprise.expensetracker.core.model.BudgetScope
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.GetAiInsightsUseCase
import com.enterprise.expensetracker.domain.usecase.GetBudgetsUseCase
import com.enterprise.expensetracker.domain.usecase.GetCurrentUserUseCase
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import com.enterprise.expensetracker.domain.usecase.SaveBudgetUseCase
import com.enterprise.expensetracker.domain.usecase.SyncExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getAiInsightsUseCase: GetAiInsightsUseCase,
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase,
    private val syncExpensesUseCase: SyncExpensesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val saveBudgetUseCase: SaveBudgetUseCase
) : BaseViewModel<DashboardState, DashboardEvent, DashboardEffect>(DashboardState()) {

    init {
        loadData()
    }

    private fun loadData() {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            combine(
                getExpensesUseCase(),
                getDynamicConfigUseCase.observeConfig(),
                getCurrentUserUseCase(),
                getBudgetsUseCase.observeAllBudgets()
            ) { expenses, config, currentUser, allBudgets ->
                DataSnapshot(expenses, config.activeAiProvider, currentUser, allBudgets)
            }.collect { snapshot ->
                updateStateFromSnapshot(snapshot)
            }
        }

        // Fetch home AI budget recommendations & alerts
        viewModelScope.launch {
            getAiInsightsUseCase.getAnomalies().collect { res ->
                if (res is Resource.Success && res.data.isNotEmpty()) {
                    setState { copy(featuredInsight = res.data.first()) }
                }
            }
        }
    }

    private fun updateStateFromSnapshot(snapshot: DataSnapshot) {
        val user = snapshot.currentUser
        val familyId = user?.familyId ?: "family_primary"

        val familyBudget = snapshot.allBudgets.firstOrNull {
            it.familyId == familyId && it.scope == BudgetScope.FAMILY
        } ?: snapshot.allBudgets.firstOrNull { it.scope == BudgetScope.FAMILY }
        val memberBudget = snapshot.allBudgets.firstOrNull {
            it.familyId == familyId && it.userId == user?.id && it.scope == BudgetScope.INDIVIDUAL
        }

        val periodView = currentState.selectedPeriodView
        val periodStartTime = getPeriodStartTime(periodView)

        // Filter expenses for current period
        val periodExpenses = snapshot.expenses.filter { it.timestamp >= periodStartTime }

        // Family spend (all family expenses in period)
        val familySpend = periodExpenses.sumOf { it.amount }

        // Member spend (expenses created by current user in period)
        val memberSpend = if (user != null) {
            periodExpenses.filter { it.createdByUserId == user.id || it.createdByUserName.equals(user.name, ignoreCase = true) }
                .sumOf { it.amount }
        } else 0.0

        val predicted = if (familySpend > 0) familySpend * 1.25 else 0.0

        setState {
            copy(
                isLoading = false,
                currentUser = user,
                familyBudget = familyBudget,
                memberBudget = memberBudget,
                familySpendInPeriod = familySpend,
                memberSpendInPeriod = memberSpend,
                predictedEndOfMonthSpend = predicted,
                recentExpenses = snapshot.expenses.take(5),
                activeAiEngine = snapshot.activeAiEngine,
                currencySymbol = "₹"
            )
        }
    }

    private fun getPeriodStartTime(view: BudgetPeriodView): Long {
        val cal = Calendar.getInstance()
        return when (view) {
            BudgetPeriodView.WEEKLY -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            BudgetPeriodView.MONTHLY -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            BudgetPeriodView.YEARLY -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
        }
    }

    override fun handleEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.RefreshData -> {
                viewModelScope.launch {
                    getDynamicConfigUseCase.refresh()
                    sendEffect(DashboardEffect.ShowToast("Refreshed cloud configuration"))
                }
            }
            is DashboardEvent.SyncData -> {
                viewModelScope.launch {
                    val result = syncExpensesUseCase()
                    if (result.isSuccess) {
                        sendEffect(DashboardEffect.ShowToast("Cloud sync complete"))
                    } else {
                        sendEffect(DashboardEffect.ShowToast("Sync queued for background"))
                    }
                }
            }
            is DashboardEvent.SelectPeriodView -> {
                setState { copy(selectedPeriodView = event.period) }
                loadData()
            }
            is DashboardEvent.SetBudgetDialogVisible -> {
                setState { copy(isBudgetDialogOpen = event.visible) }
            }
            is DashboardEvent.SaveBudgetSubmitted -> {
                viewModelScope.launch {
                    val user = currentState.currentUser
                    val familyId = user?.familyId ?: "family_primary"
                    val budgetId = if (event.isFamilyBudget) {
                        "${familyId}_FAMILY"
                    } else {
                        "${familyId}_${user?.id ?: "member"}"
                    }

                    val res = saveBudgetUseCase(
                        id = budgetId,
                        familyId = familyId,
                        userId = if (event.isFamilyBudget) null else user?.id,
                        userName = if (event.isFamilyBudget) "Family Head" else user?.name,
                        scope = if (event.isFamilyBudget) BudgetScope.FAMILY else BudgetScope.INDIVIDUAL,
                        baseAmount = event.amount,
                        periodType = event.periodType
                    )

                    if (res.isSuccess) {
                        setState { copy(isBudgetDialogOpen = false) }
                        sendEffect(DashboardEffect.ShowToast("Budget saved and synced to Atlas"))
                    } else {
                        sendEffect(DashboardEffect.ShowToast("Failed to save budget: ${res.exceptionOrNull()?.message}"))
                    }
                }
            }
            is DashboardEvent.DeleteExpense -> {
                viewModelScope.launch {
                    val result = deleteExpenseUseCase(event.id)
                    if (result.isSuccess) {
                        sendEffect(DashboardEffect.ShowToast("Expense deleted"))
                    } else {
                        sendEffect(DashboardEffect.ShowToast("Could not delete expense"))
                    }
                }
            }
        }
    }

    private data class DataSnapshot(
        val expenses: List<Expense>,
        val activeAiEngine: String,
        val currentUser: com.enterprise.expensetracker.core.model.UserProfile?,
        val allBudgets: List<com.enterprise.expensetracker.core.model.Budget>
    )
}
