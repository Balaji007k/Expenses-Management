package com.enterprise.expensetracker.core.database.dao

import com.enterprise.expensetracker.core.database.entity.ExpenseMongoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MongoDB.
 * Exposes reactive Kotlin Flows for real-time local and remote synchronization.
 */
interface ExpenseMongoDao {
    fun getAllExpensesFlow(): Flow<List<ExpenseMongoEntity>>
    fun getExpensesByCategoryFlow(categoryId: String): Flow<List<ExpenseMongoEntity>>
    suspend fun getExpenseById(id: String): ExpenseMongoEntity?
    suspend fun upsertExpense(entity: ExpenseMongoEntity)
    suspend fun upsertExpenses(entities: List<ExpenseMongoEntity>)
    suspend fun deleteExpenseById(id: String)
    suspend fun getPendingSyncExpenses(): List<ExpenseMongoEntity>
    suspend fun markAsSynced(id: String)
    suspend fun clearAll()
}
