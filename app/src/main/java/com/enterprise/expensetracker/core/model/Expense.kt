package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Domain entity representing an Expense in the enterprise system.
 * Free from any Android framework or DB engine dependencies.
 */
@Serializable
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: Category,
    val currency: Currency = Currency.USD,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val receiptUrl: String? = null,
    val isRecurring: Boolean = false,
    val isAiPredicted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val updatedAt: Long = System.currentTimeMillis()
)
