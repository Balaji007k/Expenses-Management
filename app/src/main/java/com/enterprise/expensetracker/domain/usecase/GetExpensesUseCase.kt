package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(category: Category? = null): Flow<List<Expense>> {
        return if (category != null) {
            expenseRepository.observeExpensesByCategory(category)
        } else {
            expenseRepository.observeAllExpenses()
        }
    }
}
