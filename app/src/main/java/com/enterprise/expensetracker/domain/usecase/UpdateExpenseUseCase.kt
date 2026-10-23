package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.RestockReason
import com.enterprise.expensetracker.core.model.StockStatus
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> {
        if (expense.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Expense title cannot be empty"))
        }
        if (expense.amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Expense amount must be greater than zero"))
        }
        return expenseRepository.updateExpense(expense)
    }

    suspend fun markStockEnded(expense: Expense): Result<Unit> {
        val updated = expense.copy(
            stockStatus = StockStatus.STOCK_ENDED,
            stockEndedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return expenseRepository.updateExpense(updated)
    }

    suspend fun recordRestock(
        expense: Expense,
        reason: RestockReason,
        quantityRemainingBeforeRestock: Double?
    ): Result<Unit> {
        val updated = expense.copy(
            stockStatus = StockStatus.RESTOCKED,
            lastRestockedAt = System.currentTimeMillis(),
            restockReason = reason,
            quantityRemainingBeforeRestock = if (reason == RestockReason.IN_BETWEEN) quantityRemainingBeforeRestock else 0.0,
            updatedAt = System.currentTimeMillis()
        )
        return expenseRepository.updateExpense(updated)
    }
}
