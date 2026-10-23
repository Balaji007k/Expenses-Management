package com.enterprise.expensetracker.core.ai

import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.SpendingForecast

/**
 * Unified AI Abstraction for Enterprise Predictive Analytics.
 * Enables interchangeable usage of Google Gemini, OpenAI GPT, or on-device engines.
 */
interface AiEngine {
    val providerType: AiProviderType

    suspend fun generateMonthlyForecast(
        expenses: List<Expense>,
        monthlyBudget: Double
    ): Result<SpendingForecast>

    suspend fun detectAnomalies(
        expenses: List<Expense>
    ): Result<List<AiInsight>>

    suspend fun suggestCategoryForExpense(
        title: String,
        amount: Double
    ): Result<Category>
}
