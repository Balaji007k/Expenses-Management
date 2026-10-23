package com.enterprise.expensetracker.core.model.ai

import kotlinx.serialization.Serializable

@Serializable
enum class InsightSeverity {
    INFO,
    WARNING,
    ALERT,
    OPPORTUNITY
}

@Serializable
enum class InsightType {
    ANOMALY_DETECTED,
    SPENDING_FORECAST,
    BUDGET_OPTIMIZATION,
    RECURRING_SUBSCRIPTION,
    TAX_DEDUCTIBLE_SUGGESTION
}

@Serializable
data class AiInsight(
    val id: String,
    val type: InsightType,
    val severity: InsightSeverity,
    val title: String,
    val message: String,
    val estimatedSavings: Double? = null,
    val confidenceScore: Float = 0.95f,
    val generatedByEngine: String = "Gemini",
    val timestamp: Long = System.currentTimeMillis()
)
