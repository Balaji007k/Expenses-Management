package com.enterprise.expensetracker.data.repository

import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.database.MongoDbManager
import com.enterprise.expensetracker.core.database.entity.toDomain
import com.enterprise.expensetracker.core.database.entity.toMongoEntity
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Repository implementation coordinating MongoDB Atlas persistence,
 * offline caches, and sync states.
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val mongoDbManager: MongoDbManager,
    private val dispatchers: CoroutineDispatchers
) : ExpenseRepository {

    override fun observeAllExpenses(): Flow<List<Expense>> {
        return mongoDbManager.expenseDao.getAllExpensesFlow()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun observeExpensesByCategory(category: Category): Flow<List<Expense>> {
        return mongoDbManager.expenseDao.getExpensesByCategoryFlow(category.id)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun getExpenseById(id: String): Flow<Resource<Expense>> = flow {
        emit(Resource.Loading)
        try {
            val entity = mongoDbManager.expenseDao.getExpenseById(id)
            if (entity != null) {
                emit(Resource.Success(entity.toDomain()))
            } else {
                emit(Resource.Error("Expense with ID $id not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown error occurred", e))
        }
    }.flowOn(dispatchers.io)

    override suspend fun addExpense(expense: Expense): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                mongoDbManager.expenseDao.upsertExpense(expense.toMongoEntity())
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                mongoDbManager.expenseDao.upsertExpense(expense.toMongoEntity())
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteExpense(id: String): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                mongoDbManager.expenseDao.deleteExpenseById(id)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun syncWithMongo(): Result<Unit> {
        return mongoDbManager.triggerSync()
    }
}
