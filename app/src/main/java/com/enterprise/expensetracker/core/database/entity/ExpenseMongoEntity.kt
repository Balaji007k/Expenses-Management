package com.enterprise.expensetracker.core.database.entity

import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.SyncStatus
import kotlinx.serialization.Serializable

/**
 * MongoDB / Realm document entity mapping for expenses.
 * Represents schema for MongoDB Atlas Device Sync & Local Database.
 */
@Serializable
data class ExpenseMongoEntity(
    var _id: String = "",
    var ownerId: String = "user_default",
    var title: String = "",
    var amount: Double = 0.0,
    var categoryId: String = "misc",
    var currencyCode: String = "USD",
    var timestamp: Long = 0L,
    var notes: String? = null,
    var receiptUrl: String? = null,
    var isRecurring: Boolean = false,
    var isAiPredicted: Boolean = false,
    var syncState: String = "SYNCED",
    var updatedAt: Long = 0L
)

fun Expense.toMongoEntity(ownerId: String = "enterprise_user"): ExpenseMongoEntity {
    return ExpenseMongoEntity(
        _id = id,
        ownerId = ownerId,
        title = title,
        amount = amount,
        categoryId = category.id,
        currencyCode = currency.code,
        timestamp = timestamp,
        notes = notes,
        receiptUrl = receiptUrl,
        isRecurring = isRecurring,
        isAiPredicted = isAiPredicted,
        syncState = syncStatus.name,
        updatedAt = updatedAt
    )
}

fun ExpenseMongoEntity.toDomain(): Expense {
    return Expense(
        id = _id,
        title = title,
        amount = amount,
        category = Category.fromId(categoryId),
        currency = Currency.fromCode(currencyCode),
        timestamp = timestamp,
        notes = notes,
        receiptUrl = receiptUrl,
        isRecurring = isRecurring,
        isAiPredicted = isAiPredicted,
        syncStatus = runCatching { SyncStatus.valueOf(syncState) }.getOrDefault(SyncStatus.SYNCED),
        updatedAt = updatedAt
    )
}
