package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ExpenseScope {
    FAMILY,
    INDIVIDUAL
}

@Serializable
enum class StockStatus {
    IN_STOCK,
    STOCK_ENDED,
    RESTOCKED
}

@Serializable
enum class RestockReason {
    EMPTIED,
    IN_BETWEEN
}

/**
 * Domain entity representing an Expense in the enterprise system.
 * Contains detailed quantity, catalog image, scope, and inventory restock tracking.
 */
@Serializable
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: Category,
    val currency: Currency = Currency.INR,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val receiptUrl: String? = null,
    val isRecurring: Boolean = false,
    val isAiPredicted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdByUserId: String = "user_default",
    val createdByUserName: String = "Head of Household",
    val createdByRelationship: String = "Head",
    val familyId: String = "family_primary",
    val quantity: Double = 1.0,
    val unit: String = "item",
    val scope: ExpenseScope = ExpenseScope.FAMILY,
    val stockStatus: StockStatus = StockStatus.IN_STOCK,
    val stockEndedAt: Long? = null,
    val lastRestockedAt: Long? = null,
    val restockReason: RestockReason? = null,
    val quantityRemainingBeforeRestock: Double? = null
)
