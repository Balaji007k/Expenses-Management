package com.enterprise.expensetracker.feature.expenses

import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ExpenseScope
import com.enterprise.expensetracker.core.model.ItemCatalog
import com.enterprise.expensetracker.core.model.RestockReason
import com.enterprise.expensetracker.core.model.UserProfile

data class ExpensesState(
    val isLoading: Boolean = false,
    val currentUser: UserProfile? = null,
    val familyMembers: List<UserProfile> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val catalogSuggestions: List<ItemCatalog> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedMemberId: String? = null,
    val selectedScope: ExpenseScope? = null,
    val searchQuery: String = "",
    val isAddDialogOpen: Boolean = false,
    val editingExpense: Expense? = null,
    val restockingExpense: Expense? = null,
    val currencySymbol: String = "₹"
) {
    val isHeadOfHousehold: Boolean
        get() = currentUser?.role?.name == "HEAD_OF_HOUSEHOLD" || currentUser?.relationship?.name == "HEAD"
}

sealed interface ExpensesEvent {
    data class SearchQueryChanged(val query: String) : ExpensesEvent
    data class CategoryFilterSelected(val category: Category?) : ExpensesEvent
    data class MemberFilterSelected(val memberId: String?) : ExpensesEvent
    data class ScopeFilterSelected(val scope: ExpenseScope?) : ExpensesEvent
    data class SetAddDialogVisible(val isVisible: Boolean) : ExpensesEvent
    data class StartEditingExpense(val expense: Expense?) : ExpensesEvent
    data class StartRestockingExpense(val expense: Expense?) : ExpensesEvent
    data class AddExpenseSubmitted(
        val title: String,
        val amount: Double,
        val category: Category,
        val quantity: Double,
        val unit: String,
        val scope: ExpenseScope,
        val notes: String?,
        val imageUrl: String?
    ) : ExpensesEvent
    data class EditExpenseSubmitted(
        val id: String,
        val title: String,
        val amount: Double,
        val category: Category,
        val quantity: Double,
        val unit: String,
        val scope: ExpenseScope,
        val notes: String?,
        val imageUrl: String?
    ) : ExpensesEvent
    data class StockEndedSubmitted(val expense: Expense) : ExpensesEvent
    data class RestockSubmitted(val expense: Expense, val reason: RestockReason, val quantityRemaining: Double?) : ExpensesEvent
    data class DeleteExpense(val id: String) : ExpensesEvent
    data object Refresh : ExpensesEvent
}

sealed interface ExpensesEffect {
    data class ShowMessage(val text: String) : ExpensesEffect
}
