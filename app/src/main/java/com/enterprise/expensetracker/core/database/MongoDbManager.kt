package com.enterprise.expensetracker.core.database

import com.enterprise.expensetracker.core.database.dao.ExpenseMongoDao
import com.enterprise.expensetracker.core.database.dao.UserMongoDao
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
    val activeConnectionUri: StateFlow<String>
    val expenseDao: ExpenseMongoDao
    val userDao: UserMongoDao

    suspend fun initialize(appId: String, apiKey: String? = null)
    suspend fun connectWithUri(uri: String): Result<Unit>
    suspend fun triggerSync(): Result<Unit>
    suspend fun disconnect()
}
