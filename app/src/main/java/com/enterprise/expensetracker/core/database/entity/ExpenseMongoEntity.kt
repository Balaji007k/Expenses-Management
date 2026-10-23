package com.enterprise.expensetracker.core.database.entity

import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ExpenseScope
import com.enterprise.expensetracker.core.model.RestockReason
import com.enterprise.expensetracker.core.model.StockStatus
import com.enterprise.expensetracker.core.model.SyncStatus
import kotlinx.serialization.Serializable

/**
 * MongoDB document entity mapping for expenses in Atlas 'Expense' collection.
 */
@Serializable
data class ExpenseMongoEntity(
    var _id: String = "",
    var ownerId: String = "user_default",
    var title: String = "",
    var amount: Double = 0.0,
    var categoryId: String = "misc",
    var currencyCode: String = "INR",
    var timestamp: Long = 0L,
    var notes: String? = null,
    var receiptUrl: String? = null,
    var isRecurring: Boolean = false,
    var isAiPredicted: Boolean = false,
    var syncState: String = "SYNCED",
    var updatedAt: Long = 0L,
    var createdByUserId: String = "user_default",
    var createdByUserName: String = "Head of Household",
    var createdByRelationship: String = "Head",
    var familyId: String = "family_primary",
    var quantity: Double = 1.0,
    var unit: String = "item",
    var scope: String = "FAMILY",
    var stockStatus: String = "IN_STOCK",
    var stockEndedAt: Long? = null,
    var lastRestockedAt: Long? = null,
    var restockReason: String? = null,
    var quantityRemainingBeforeRestock: Double? = null
)

fun Expense.toMongoEntity(ownerId: String = "household_user"): ExpenseMongoEntity {
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
        updatedAt = updatedAt,
        createdByUserId = createdByUserId,
        createdByUserName = createdByUserName,
        createdByRelationship = createdByRelationship,
        familyId = familyId,
        quantity = quantity,
        unit = unit,
        scope = scope.name,
        stockStatus = stockStatus.name,
        stockEndedAt = stockEndedAt,
        lastRestockedAt = lastRestockedAt,
        restockReason = restockReason?.name,
        quantityRemainingBeforeRestock = quantityRemainingBeforeRestock
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
        updatedAt = updatedAt,
        createdByUserId = createdByUserId,
        createdByUserName = createdByUserName,
        createdByRelationship = createdByRelationship,
        familyId = familyId,
        quantity = quantity,
        unit = unit.ifBlank { "item" },
        scope = runCatching { ExpenseScope.valueOf(scope) }.getOrDefault(ExpenseScope.FAMILY),
        stockStatus = runCatching { StockStatus.valueOf(stockStatus) }.getOrDefault(StockStatus.IN_STOCK),
        stockEndedAt = stockEndedAt,
        lastRestockedAt = lastRestockedAt,
        restockReason = restockReason?.let { runCatching { RestockReason.valueOf(it) }.getOrNull() },
        quantityRemainingBeforeRestock = quantityRemainingBeforeRestock
    )
}
