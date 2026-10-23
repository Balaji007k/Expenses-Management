package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Dynamic Configuration fetched dynamically at runtime from Firebase Remote Config.
 * Enables live feature toggling, dynamic threshold adjustments, and AI provider routing
 * without requiring Google Play Store releases.
 */
@Serializable
data class DynamicAppConfig(
    val activeAiProvider: String = "GEMINI",
    val isAiInsightsEnabled: Boolean = true,
    val isAnomalyDetectionEnabled: Boolean = true,
    val defaultCurrencyCode: String = "INR",
    val maxSingleExpenseThreshold: Double = 50000.0,
    val syncIntervalMinutes: Long = 15L,
    val mongoClusterRegion: String = "us-east-1",
    val isReceiptScanEnabled: Boolean = true,
    val welcomeMessage: String = "Welcome to Home Expense Tracker",
    val dbConnectionUri: String = "mongodb://teabuff_db_user:1XPvy4K8hHmzZqfg@ac-cbt44uz-shard-00-00.fithesf.mongodb.net:27017,ac-cbt44uz-shard-00-01.fithesf.mongodb.net:27017,ac-cbt44uz-shard-00-02.fithesf.mongodb.net:27017/ExpensesManagement?ssl=true&replicaSet=atlas-12mqms-shard-0&authSource=admin"
)
