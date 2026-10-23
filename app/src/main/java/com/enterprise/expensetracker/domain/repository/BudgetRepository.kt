package com.enterprise.expensetracker.domain.repository

import com.enterprise.expensetracker.core.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeAllBudgets(): Flow<List<Budget>>
    fun observeFamilyBudget(familyId: String): Flow<Budget?>
    fun observeMemberBudget(familyId: String, userId: String): Flow<Budget?>
    suspend fun saveBudget(budget: Budget): Result<Unit>
    suspend fun deleteBudget(id: String): Result<Unit>
}
