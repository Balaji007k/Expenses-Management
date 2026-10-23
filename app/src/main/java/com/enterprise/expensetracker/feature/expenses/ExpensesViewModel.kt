package com.enterprise.expensetracker.feature.expenses

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ExpenseScope
import com.enterprise.expensetracker.core.model.ItemCatalog
import com.enterprise.expensetracker.core.model.SyncStatus
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole
import com.enterprise.expensetracker.domain.usecase.AddExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.GetCatalogItemsUseCase
import com.enterprise.expensetracker.domain.usecase.GetCurrentUserUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import com.enterprise.expensetracker.domain.usecase.GetFamilyMembersUseCase
import com.enterprise.expensetracker.domain.usecase.SyncExpensesUseCase
import com.enterprise.expensetracker.domain.usecase.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
    private val getCatalogItemsUseCase: GetCatalogItemsUseCase,
    private val syncExpensesUseCase: SyncExpensesUseCase
) : BaseViewModel<ExpensesState, ExpensesEvent, ExpensesEffect>(ExpensesState()) {

    init {
        observeData()
    }

    private fun observeData() {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            combine(
                getExpensesUseCase(),
                getCurrentUserUseCase(),
                getFamilyMembersUseCase(),
                getCatalogItemsUseCase()
            ) { expenses, currentUser, familyMembers, catalogItems ->
                StateBundle(expenses, currentUser, familyMembers, catalogItems)
            }.collect { bundle ->
                val user = bundle.currentUser

                // Family Member can view their own expenses and family-shared expenses
                // Head of Household can view everything
                val visibleExpenses = if (user?.role == UserRole.FAMILY_MEMBER) {
                    bundle.expenses.filter {
                        it.createdByUserId == user.id ||
                                it.createdByUserName.equals(user.name, ignoreCase = true) ||
                                it.scope == ExpenseScope.FAMILY
                    }
                } else {
                    bundle.expenses
                }

                setState {
                    copy(
                        isLoading = false,
                        currentUser = user,
                        familyMembers = bundle.familyMembers,
                        catalogSuggestions = bundle.catalogItems,
                        expenses = visibleExpenses,
                        filteredExpenses = filterList(
                            list = visibleExpenses,
                            category = selectedCategory,
                            memberId = selectedMemberId,
                            scope = selectedScope,
                            query = searchQuery
                        ),
                        currencySymbol = "₹"
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
                        filteredExpenses = filterList(expenses, selectedCategory, selectedMemberId, selectedScope, event.query)
                    )
                }
            }
            is ExpensesEvent.CategoryFilterSelected -> {
                setState {
                    copy(
                        selectedCategory = event.category,
                        filteredExpenses = filterList(expenses, event.category, selectedMemberId, selectedScope, searchQuery)
                    )
                }
            }
            is ExpensesEvent.MemberFilterSelected -> {
                setState {
                    copy(
                        selectedMemberId = event.memberId,
                        filteredExpenses = filterList(expenses, selectedCategory, event.memberId, selectedScope, searchQuery)
                    )
                }
            }
            is ExpensesEvent.ScopeFilterSelected -> {
                setState {
                    copy(
                        selectedScope = event.scope,
                        filteredExpenses = filterList(expenses, selectedCategory, selectedMemberId, event.scope, searchQuery)
                    )
                }
            }
            is ExpensesEvent.SetAddDialogVisible -> {
                setState { copy(isAddDialogOpen = event.isVisible, editingExpense = null) }
            }
            is ExpensesEvent.StartEditingExpense -> {
                setState { copy(editingExpense = event.expense, isAddDialogOpen = event.expense != null) }
            }
            is ExpensesEvent.StartRestockingExpense -> {
                setState { copy(restockingExpense = event.expense) }
            }
            is ExpensesEvent.AddExpenseSubmitted -> {
                viewModelScope.launch {
                    val user = currentState.currentUser
                    val newExpense = Expense(
                        id = UUID.randomUUID().toString(),
                        title = event.title,
                        amount = event.amount,
                        category = event.category,
                        quantity = event.quantity,
                        unit = event.unit,
                        scope = event.scope,
                        notes = event.notes,
                        receiptUrl = event.imageUrl,
                        createdByUserId = user?.id ?: "user_default",
                        createdByUserName = user?.name ?: "Family Member",
                        createdByRelationship = user?.relationship?.displayName ?: "Member",
                        familyId = user?.familyId ?: "family_primary",
                        syncStatus = SyncStatus.SYNCED
                    )
                    val result = addExpenseUseCase(newExpense)
                    if (result.isSuccess) {
                        setState { copy(isAddDialogOpen = false) }
                        sendEffect(ExpensesEffect.ShowMessage("Added '${event.title}' (${event.quantity} ${event.unit})"))
                    } else {
                        sendEffect(ExpensesEffect.ShowMessage("Failed: ${result.exceptionOrNull()?.message}"))
                    }
                }
            }
            is ExpensesEvent.EditExpenseSubmitted -> {
                viewModelScope.launch {
                    val existing = currentState.expenses.firstOrNull { it.id == event.id }
                    if (existing != null) {
                        val updated = existing.copy(
                            title = event.title,
                            amount = event.amount,
                            category = event.category,
                            quantity = event.quantity,
                            unit = event.unit,
                            scope = event.scope,
                            notes = event.notes,
                            receiptUrl = event.imageUrl,
                            updatedAt = System.currentTimeMillis()
                        )
                        val res = updateExpenseUseCase(updated)
                        if (res.isSuccess) {
                            setState { copy(isAddDialogOpen = false, editingExpense = null) }
                            sendEffect(ExpensesEffect.ShowMessage("Updated '${event.title}'"))
                        } else {
                            sendEffect(ExpensesEffect.ShowMessage("Update failed: ${res.exceptionOrNull()?.message}"))
                        }
                    }
                }
            }
            is ExpensesEvent.StockEndedSubmitted -> {
                viewModelScope.launch {
                    val res = updateExpenseUseCase.markStockEnded(event.expense)
                    if (res.isSuccess) {
                        sendEffect(ExpensesEffect.ShowMessage("Marked '${event.expense.title}' stock as ended"))
                    }
                }
            }
            is ExpensesEvent.RestockSubmitted -> {
                viewModelScope.launch {
                    val res = updateExpenseUseCase.recordRestock(event.expense, event.reason, event.quantityRemaining)
                    if (res.isSuccess) {
                        setState { copy(restockingExpense = null) }
                        sendEffect(ExpensesEffect.ShowMessage("Restocked '${event.expense.title}'"))
                    }
                }
            }
            is ExpensesEvent.DeleteExpense -> {
                // Strictly enforce Head of Household check
                if (!currentState.isHeadOfHousehold) {
                    sendEffect(ExpensesEffect.ShowMessage("Permission denied: Only the Head of Household can delete expenses."))
                    return
                }
                viewModelScope.launch {
                    val result = deleteExpenseUseCase(event.id)
                    if (result.isSuccess) {
                        sendEffect(ExpensesEffect.ShowMessage("Expense deleted from MongoDB Atlas"))
                    } else {
                        sendEffect(ExpensesEffect.ShowMessage("Could not delete expense"))
                    }
                }
            }
            is ExpensesEvent.Refresh -> {
                viewModelScope.launch {
                    syncExpensesUseCase()
                    sendEffect(ExpensesEffect.ShowMessage("Synchronized with MongoDB Atlas"))
                }
            }
        }
    }

    private fun filterList(
        list: List<Expense>,
        category: Category?,
        memberId: String?,
        scope: ExpenseScope?,
        query: String
    ): List<Expense> {
        return list.filter { expense ->
            val matchesCategory = category == null || expense.category == category
            val matchesMember = memberId == null || expense.createdByUserId == memberId
            val matchesScope = scope == null || expense.scope == scope
            val matchesQuery = query.isBlank() ||
                    expense.title.contains(query, ignoreCase = true) ||
                    (expense.notes?.contains(query, ignoreCase = true) == true) ||
                    expense.category.displayName.contains(query, ignoreCase = true) ||
                    expense.createdByUserName.contains(query, ignoreCase = true)

            matchesCategory && matchesMember && matchesScope && matchesQuery
        }
    }

    private data class StateBundle(
        val expenses: List<Expense>,
        val currentUser: UserProfile?,
        val familyMembers: List<UserProfile>,
        val catalogItems: List<ItemCatalog>
    )
}
