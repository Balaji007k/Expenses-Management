package com.enterprise.expensetracker.feature.expenses

import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense

data class ExpensesState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val isAddDialogOpen: Boolean = false,
    val currencySymbol: String = "$"
)

sealed interface ExpensesEvent {
    data class SearchQueryChanged(val query: String) : ExpensesEvent
    data class CategoryFilterSelected(val category: Category?) : ExpensesEvent
    data class AddExpenseSubmitted(val title: String, val amount: Double, val category: Category, val notes: String?) : ExpensesEvent
    data class DeleteExpense(val id: String) : ExpensesEvent
    data class SetAddDialogVisible(val isVisible: Boolean) : ExpensesEvent
}

sealed interface ExpensesEffect {
    data class ShowMessage(val text: String) : ExpensesEffect
}
