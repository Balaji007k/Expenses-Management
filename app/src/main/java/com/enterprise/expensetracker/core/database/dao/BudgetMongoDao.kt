package com.enterprise.expensetracker.core.database.dao

import com.enterprise.expensetracker.core.database.entity.BudgetMongoEntity
import kotlinx.coroutines.flow.Flow

interface BudgetMongoDao {
    fun getAllBudgetsFlow(): Flow<List<BudgetMongoEntity>>
    fun getFamilyBudgetFlow(familyId: String): Flow<BudgetMongoEntity?>
    fun getMemberBudgetFlow(familyId: String, userId: String): Flow<BudgetMongoEntity?>
    suspend fun getBudgetById(id: String): BudgetMongoEntity?
    suspend fun upsertBudget(entity: BudgetMongoEntity)
    suspend fun deleteBudgetById(id: String)
}
