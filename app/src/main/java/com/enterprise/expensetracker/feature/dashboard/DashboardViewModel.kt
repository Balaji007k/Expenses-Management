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

    private var demoIndex = 0

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
            is DashboardEvent.AddDemoExpense -> {
                viewModelScope.launch {
                    val homeDemoList = listOf(
                        Expense(
                            title = "Weekly Groceries & Vegetables",
                            amount = 2450.00,
                            category = Category.GROCERIES_AND_FOOD,
                            currency = Currency.INR,
                            notes = "Supermarket fresh food, milk and vegetables",
                            syncStatus = SyncStatus.PENDING_SYNC
                        ),
                        Expense(
                            title = "Electricity & Water Bill",
                            amount = 1850.00,
                            category = Category.UTILITIES_AND_BILLS,
                            currency = Currency.INR,
                            notes = "Monthly state electricity board",
                            syncStatus = SyncStatus.PENDING_SYNC
                        ),
                        Expense(
                            title = "Home High-Speed Wi-Fi",
                            amount = 999.00,
                            category = Category.INTERNET_AND_MOBILE,
                            currency = Currency.INR,
                            notes = "Fiber broadband monthly bill",
                            syncStatus = SyncStatus.PENDING_SYNC
                        ),
                        Expense(
                            title = "Kids School Supplies & Books",
                            amount = 1200.00,
                            category = Category.KIDS_AND_EDUCATION,
                            currency = Currency.INR,
                            notes = "Stationery, notebooks and school project",
                            syncStatus = SyncStatus.PENDING_SYNC
                        ),
                        Expense(
                            title = "Family Dinner & Weekend Treat",
                            amount = 1650.00,
                            category = Category.DINING_AND_OUTING,
                            currency = Currency.INR,
                            notes = "Sunday family dinner",
                            syncStatus = SyncStatus.PENDING_SYNC
                        )
                    )

                    val demo = homeDemoList[demoIndex % homeDemoList.size]
                    demoIndex++
                    addExpenseUseCase(demo)
                    sendEffect(DashboardEffect.ShowToast("Added: ${demo.title}"))
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
