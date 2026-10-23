package com.enterprise.expensetracker.core.database.entity

import com.enterprise.expensetracker.core.model.Budget
import com.enterprise.expensetracker.core.model.BudgetPeriod
import com.enterprise.expensetracker.core.model.BudgetScope
import kotlinx.serialization.Serializable

@Serializable
data class BudgetMongoEntity(
    var _id: String = "",
    var familyId: String = "family_primary",
    var userId: String? = null,
    var userName: String? = null,
    var scope: String = "FAMILY",
    var baseAmount: Double = 50000.0,
    var periodType: String = "MONTHLY",
    var updatedAt: Long = 0L
)

fun Budget.toMongoEntity(): BudgetMongoEntity {
    return BudgetMongoEntity(
        _id = id,
        familyId = familyId,
        userId = userId,
        userName = userName,
        scope = scope.name,
        baseAmount = baseAmount,
        periodType = periodType.name,
        updatedAt = updatedAt
    )
}

fun BudgetMongoEntity.toDomain(): Budget {
    return Budget(
        id = _id,
        familyId = familyId,
        userId = userId,
        userName = userName,
        scope = runCatching { BudgetScope.valueOf(scope) }.getOrDefault(BudgetScope.FAMILY),
        baseAmount = baseAmount,
        periodType = runCatching { BudgetPeriod.valueOf(periodType) }.getOrDefault(BudgetPeriod.MONTHLY),
        updatedAt = updatedAt
    )
}
