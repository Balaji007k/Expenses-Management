package com.enterprise.expensetracker.feature.expenses

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.SyncStatus
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole
import com.enterprise.expensetracker.domain.usecase.AddExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.enterprise.expensetracker.domain.usecase.GetCurrentUserUseCase
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import com.enterprise.expensetracker.domain.usecase.GetFamilyMembersUseCase
import com.enterprise.expensetracker.domain.usecase.SyncExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
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
                getDynamicConfigUseCase.observeConfig(),
                getCurrentUserUseCase(),
                getFamilyMembersUseCase()
            ) { expenses, config, currentUser, familyMembers ->
                StateBundle(expenses, config.defaultCurrencyCode, currentUser, familyMembers)
            }.collect { bundle ->
                val currency = Currency.fromCode(bundle.currencyCode)
                val user = bundle.currentUser

                // Filter base expenses according to user role:
                // Family Member can ONLY see their own expenses
                // Head of Household can see all family expenses
                val visibleExpenses = if (user?.role == UserRole.FAMILY_MEMBER) {
                    bundle.expenses.filter {
                        it.createdByUserId == user.id || it.createdByUserName.equals(user.name, ignoreCase = true)
                    }
                } else {
                    bundle.expenses
                }

                setState {
                    copy(
                        isLoading = false,
                        currentUser = user,
                        familyMembers = bundle.familyMembers,
                        expenses = visibleExpenses,
                        filteredExpenses = filterList(
                            list = visibleExpenses,
                            category = selectedCategory,
                            memberId = selectedMemberId,
                            query = searchQuery
                        ),
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
                        filteredExpenses = filterList(expenses, selectedCategory, selectedMemberId, event.query)
                    )
                }
            }
            is ExpensesEvent.CategoryFilterSelected -> {
                setState {
                    copy(
                        selectedCategory = event.category,
                        filteredExpenses = filterList(expenses, event.category, selectedMemberId, searchQuery)
                    )
                }
            }
            is ExpensesEvent.MemberFilterSelected -> {
                setState {
                    copy(
                        selectedMemberId = event.memberId,
                        filteredExpenses = filterList(expenses, selectedCategory, event.memberId, searchQuery)
                    )
                }
            }
            is ExpensesEvent.SetAddDialogVisible -> {
                setState { copy(isAddDialogOpen = event.isVisible) }
            }
            is ExpensesEvent.AddExpenseSubmitted -> {
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
                    setState { copy(isAddDialogOpen = false) }
                    sendEffect(ExpensesEffect.ShowMessage("Expense added by ${newExpense.createdByUserName}"))
                }
            }
            is ExpensesEvent.DeleteExpense -> {
                viewModelScope.launch {
                    val user = currentState.currentUser
                    if (user?.isHeadOfHousehold != true) {
                        sendEffect(ExpensesEffect.ShowMessage("Permission denied: Only the Head of Household can delete expenses"))
                        return@launch
                    }

                    deleteExpenseUseCase(event.id)
                    sendEffect(ExpensesEffect.ShowMessage("Expense deleted"))
                }
            }
            is ExpensesEvent.Refresh -> {
                viewModelScope.launch {
                    val result = syncExpensesUseCase()
                    if (result.isSuccess) {
                        sendEffect(ExpensesEffect.ShowMessage("Expenses refreshed from MongoDB Atlas"))
                    } else {
                        sendEffect(ExpensesEffect.ShowMessage("Expenses refreshed"))
                    }
                }
            }
        }
    }

    private fun filterList(
        list: List<Expense>,
        category: Category?,
        memberId: String?,
        query: String
    ): List<Expense> {
        return list.filter { expense ->
            val matchesCategory = category == null || expense.category == category
            val matchesMember = memberId == null || expense.createdByUserId == memberId
            val matchesQuery = query.isBlank() ||
                expense.title.contains(query, ignoreCase = true) ||
                (expense.notes?.contains(query, ignoreCase = true) == true) ||
                expense.createdByUserName.contains(query, ignoreCase = true)
            matchesCategory && matchesMember && matchesQuery
        }
    }

    private data class StateBundle(
        val expenses: List<Expense>,
        val currencyCode: String,
        val currentUser: UserProfile?,
        val familyMembers: List<UserProfile>
    )
}
