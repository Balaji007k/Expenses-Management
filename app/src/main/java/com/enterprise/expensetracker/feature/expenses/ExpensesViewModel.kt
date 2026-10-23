package com.enterprise.expensetracker.feature.expenses

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.SyncStatus
import com.enterprise.expensetracker.domain.usecase.AddExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase
) : BaseViewModel<ExpensesState, ExpensesEvent, ExpensesEffect>(ExpensesState()) {

    init {
        observeData()
    }

    private fun observeData() {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            combine(
                getExpensesUseCase(),
                getDynamicConfigUseCase.observeConfig()
            ) { expenses, config ->
                Pair(expenses, config)
            }.collect { (expenses, config) ->
                val currency = Currency.fromCode(config.defaultCurrencyCode)
                setState {
                    copy(
                        isLoading = false,
                        expenses = expenses,
                        filteredExpenses = filterList(expenses, selectedCategory, searchQuery),
                        currencySymbol = currency.symbol
                    )
                }
            }
        }
    }

    override fun handleEvent(event: ExpensesEvent) {
        when (event) {
            is ExpensesEvent.SearchQueryChanged -> {
                setState {
                    copy(
                        searchQuery = event.query,
                        filteredExpenses = filterList(expenses, selectedCategory, event.query)
                    )
                }
            }
            is ExpensesEvent.CategoryFilterSelected -> {
                setState {
                    copy(
                        selectedCategory = event.category,
                        filteredExpenses = filterList(expenses, event.category, searchQuery)
                    )
                }
            }
            is ExpensesEvent.SetAddDialogVisible -> {
                setState { copy(isAddDialogOpen = event.isVisible) }
            }
            is ExpensesEvent.AddExpenseSubmitted -> {
                viewModelScope.launch {
                    val newExpense = Expense(
                        title = event.title,
                        amount = event.amount,
                        category = event.category,
                        notes = event.notes,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                    addExpenseUseCase(newExpense)
                    setState { copy(isAddDialogOpen = false) }
                    sendEffect(ExpensesEffect.ShowMessage("Expense added successfully"))
                }
            }
            is ExpensesEvent.DeleteExpense -> {
                viewModelScope.launch {
                    deleteExpenseUseCase(event.id)
                    sendEffect(ExpensesEffect.ShowMessage("Expense deleted"))
                }
            }
        }
    }

    private fun filterList(
        list: List<Expense>,
        category: Category?,
        query: String
    ): List<Expense> {
        return list.filter { expense ->
            val matchesCategory = category == null || expense.category == category
            val matchesQuery = query.isBlank() ||
                expense.title.contains(query, ignoreCase = true) ||
                (expense.notes?.contains(query, ignoreCase = true) == true)
            matchesCategory && matchesQuery
        }
    }
}
