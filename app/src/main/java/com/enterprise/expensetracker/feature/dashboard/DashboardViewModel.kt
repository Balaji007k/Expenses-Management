package com.enterprise.expensetracker.feature.dashboard

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.SyncStatus
import com.enterprise.expensetracker.domain.usecase.AddExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.GetAiInsightsUseCase
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
    private val syncExpensesUseCase: SyncExpensesUseCase
) : BaseViewModel<DashboardState, DashboardEvent, DashboardEffect>(DashboardState()) {

    init {
        loadData()
    }

    private fun loadData() {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            combine(
                getExpensesUseCase(),
                getDynamicConfigUseCase.observeConfig()
            ) { expenses, config ->
                val total = expenses.sumOf { it.amount }
                val currency = Currency.fromCode(config.defaultCurrencyCode)
                Pair(expenses, config)
            }.collect { (expenses, config) ->
                val total = expenses.sumOf { it.amount }
                val currency = Currency.fromCode(config.defaultCurrencyCode)
                val predicted = if (total > 0) total * 1.25 else 0.0

                setState {
                    copy(
                        isLoading = false,
                        totalSpendThisMonth = total,
                        predictedEndOfMonthSpend = predicted,
                        recentExpenses = expenses.take(5),
                        activeAiEngine = config.activeAiProvider,
                        currencySymbol = currency.symbol
                    )
                }
            }
        }

        // Fetch AI prediction & anomaly insights
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
                    sendEffect(DashboardEffect.ShowToast("Refreshed Firebase configuration"))
                }
            }
            is DashboardEvent.TriggerMongoSync -> {
                viewModelScope.launch {
                    setState { copy(mongoSyncState = "Syncing...") }
                    val result = syncExpensesUseCase()
                    if (result.isSuccess) {
                        setState { copy(mongoSyncState = "Synced") }
                        sendEffect(DashboardEffect.ShowToast("MongoDB Atlas sync complete"))
                    } else {
                        setState { copy(mongoSyncState = "Offline") }
                        sendEffect(DashboardEffect.ShowToast("Sync queued for background execution"))
                    }
                }
            }
            is DashboardEvent.AddDemoExpense -> {
                viewModelScope.launch {
                    val demo = Expense(
                        title = "AWS Cloud Infrastructure",
                        amount = 349.50,
                        category = Category.SOFTWARE_AND_TECH,
                        currency = Currency.USD,
                        isRecurring = true,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                    addExpenseUseCase(demo)
                    sendEffect(DashboardEffect.ShowToast("Added sample expense: AWS Cloud"))
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
