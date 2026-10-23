package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return expenseRepository.deleteExpense(id)
    }
}
