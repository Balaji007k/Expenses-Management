package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class SyncExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return expenseRepository.syncWithMongo()
    }
}
