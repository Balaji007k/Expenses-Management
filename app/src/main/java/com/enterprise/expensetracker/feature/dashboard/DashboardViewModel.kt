package com.enterprise.expensetracker.feature.dashboard

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.SyncStatus
import com.enterprise.expensetracker.core.model.UserRole
import com.enterprise.expensetracker.domain.usecase.AddExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.GetAiInsightsUseCase
import com.enterprise.expensetracker.domain.usecase.GetCurrentUserUseCase
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import com.enterprise.expensetracker.domain.usecase.SyncExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getAiInsightsUseCase: GetAiInsightsUseCase,
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase,
    private val syncExpensesUseCase: SyncExpensesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
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
                getCurrentUserUseCase()
            ) { expenses, config, currentUser ->
                Triple(expenses, config, currentUser)
            }.collect { (expenses, config, currentUser) ->
                val visibleExpenses = if (currentUser?.role == UserRole.FAMILY_MEMBER) {
                    expenses.filter {
                        it.createdByUserId == currentUser.id || it.createdByUserName.equals(currentUser.name, ignoreCase = true)
                    }
                } else {
                    expenses
                }

                val total = visibleExpenses.sumOf { it.amount }
                val currency = Currency.fromCode(config.defaultCurrencyCode)
                val predicted = if (total > 0) total * 1.25 else 0.0

                setState {
                    copy(
                        isLoading = false,
                        currentUser = currentUser,
                        totalSpendThisMonth = total,
                        predictedEndOfMonthSpend = predicted,
                        recentExpenses = visibleExpenses.take(5),
                        activeAiEngine = config.activeAiProvider,
                        currencySymbol = currency.symbol
                    )
                }
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
            is DashboardEvent.AddExpenseSubmitted -> {
                viewModelScope.launch {
                    val user = currentState.currentUser
                    val newExpense = Expense(
                        title = event.title,
                        amount = event.amount,
                        category = event.category,
                        currency = Currency.INR,
                        notes = event.notes,
                        syncStatus = SyncStatus.PENDING_SYNC,
                        createdByUserId = user?.id ?: "user_default",
                        createdByUserName = user?.name ?: "Family Head",
                        createdByRelationship = user?.relationship?.displayName ?: "Head",
                        familyId = user?.familyId ?: "family_primary"
                    )
                    addExpenseUseCase(newExpense)
                    sendEffect(DashboardEffect.ShowToast("Expense added by ${newExpense.createdByUserName}"))
                }
            }
            is DashboardEvent.DeleteExpense -> {
                viewModelScope.launch {
                    deleteExpenseUseCase(event.id)
                    sendEffect(DashboardEffect.ShowToast("Expense removed"))
                }
            }
        }
    }
}
