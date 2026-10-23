package com.enterprise.expensetracker.data.repository

import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.database.MongoDbManager
import com.enterprise.expensetracker.core.database.entity.toDomain
import com.enterprise.expensetracker.core.database.entity.toMongoEntity
import com.enterprise.expensetracker.core.model.Budget
import com.enterprise.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val mongoDbManager: MongoDbManager,
    private val dispatchers: CoroutineDispatchers
) : BudgetRepository {

    override fun observeAllBudgets(): Flow<List<Budget>> {
        return mongoDbManager.budgetDao.getAllBudgetsFlow()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun observeFamilyBudget(familyId: String): Flow<Budget?> {
        return mongoDbManager.budgetDao.getFamilyBudgetFlow(familyId)
            .map { it?.toDomain() }
            .flowOn(dispatchers.io)
    }

    override fun observeMemberBudget(familyId: String, userId: String): Flow<Budget?> {
        return mongoDbManager.budgetDao.getMemberBudgetFlow(familyId, userId)
            .map { it?.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun saveBudget(budget: Budget): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                mongoDbManager.budgetDao.upsertBudget(budget.toMongoEntity())
                Result.success(Unit)
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
    }

    override suspend fun deleteBudget(id: String): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                mongoDbManager.budgetDao.deleteBudgetById(id)
                Result.success(Unit)
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
    }
}
