package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> {
        if (expense.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Expense title cannot be empty"))
        }
        if (expense.amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Expense amount must be greater than zero"))
        }
        return expenseRepository.addExpense(expense)
    }
}
