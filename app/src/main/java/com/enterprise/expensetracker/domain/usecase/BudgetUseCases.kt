package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.Budget
import com.enterprise.expensetracker.core.model.BudgetPeriod
import com.enterprise.expensetracker.core.model.BudgetScope
import com.enterprise.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    fun observeFamilyBudget(familyId: String): Flow<Budget?> {
        return budgetRepository.observeFamilyBudget(familyId)
    }

    fun observeMemberBudget(familyId: String, userId: String): Flow<Budget?> {
        return budgetRepository.observeMemberBudget(familyId, userId)
    }

    fun observeAllBudgets(): Flow<List<Budget>> {
        return budgetRepository.observeAllBudgets()
    }
}

class SaveBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(
        id: String,
        familyId: String,
        userId: String?,
        userName: String?,
        scope: BudgetScope,
        baseAmount: Double,
        periodType: BudgetPeriod
    ): Result<Unit> {
        val budget = Budget(
            id = id,
            familyId = familyId,
            userId = userId,
            userName = userName,
            scope = scope,
            baseAmount = baseAmount,
            periodType = periodType,
            updatedAt = System.currentTimeMillis()
        )
        return budgetRepository.saveBudget(budget)
    }
}
