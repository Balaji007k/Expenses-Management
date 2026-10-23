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
    val defaultCurrencyCode: String = "USD",
    val maxSingleExpenseThreshold: Double = 50000.0,
    val syncIntervalMinutes: Long = 15L,
    val mongoClusterRegion: String = "us-east-1",
    val isReceiptScanEnabled: Boolean = true,
    val welcomeMessage: String = "Welcome to Enterprise Expense Management"
)
