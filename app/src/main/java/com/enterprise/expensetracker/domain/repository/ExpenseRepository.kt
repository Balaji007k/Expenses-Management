package com.enterprise.expensetracker.domain.repository

import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * Domain boundary contract for Expense operations.
 * Decouples domain logic from database (MongoDB) and network infrastructure.
 */
interface ExpenseRepository {
    fun observeAllExpenses(): Flow<List<Expense>>
    fun observeExpensesByCategory(category: Category): Flow<List<Expense>>
    fun getExpenseById(id: String): Flow<Resource<Expense>>
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun updateExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(id: String): Result<Unit>
    suspend fun syncWithMongo(): Result<Unit>
}
