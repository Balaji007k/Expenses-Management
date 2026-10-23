package com.enterprise.expensetracker.core.database

import com.enterprise.expensetracker.core.database.dao.ExpenseMongoDao
import kotlinx.coroutines.flow.StateFlow

enum class MongoConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    SYNCING,
    ERROR
}

/**
 * Enterprise MongoDB Connection & Sync Lifecycle Manager.
 * Orchestrates connection to MongoDB Atlas, local database storage,
 * and background bidirectional sync.
 */
interface MongoDbManager {
    val connectionState: StateFlow<MongoConnectionState>
    val expenseDao: ExpenseMongoDao

    suspend fun initialize(appId: String, apiKey: String? = null)
    suspend fun triggerSync(): Result<Unit>
    suspend fun disconnect()
}
