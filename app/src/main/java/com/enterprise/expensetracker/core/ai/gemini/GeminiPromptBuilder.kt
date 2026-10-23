package com.enterprise.expensetracker.core.ai.gemini

import com.enterprise.expensetracker.core.model.Expense

object GeminiPromptBuilder {

    fun buildForecastPrompt(expenses: List<Expense>, monthlyBudget: Double): String {
        val expenseSummary = expenses.take(30).joinToString(separator = "\n") {
            "- ${it.title}: ${it.currency.symbol}${it.amount} (${it.category.displayName}) on ${java.util.Date(it.timestamp)}"
        }

        return """
            You are an enterprise financial forecasting AI assistant.
            Analyze the following historical and recent expenses:
            $expenseSummary
            
            Current Monthly Budget: $$monthlyBudget
            
            Task:
            1. Calculate predicted total spend by end of current month.
            2. Compute projected daily burn rate.
            3. Highlight key spending categories driving cost changes.
            4. Provide concise executive financial summary narrative.
            
            Respond in valid JSON format matching SpendingForecast schema:
            {
              "predictedEndOfMonthSpend": 0.0,
              "predictedBurnRatePerDay": 0.0,
              "confidenceRate": 0.92,
              "summaryNarrative": "string narrative"
            }
        """.trimIndent()
    }

    fun buildAnomalyPrompt(expenses: List<Expense>): String {
        val expenseSummary = expenses.take(30).joinToString(separator = "\n") {
            "- ID ${it.id}: ${it.title} - ${it.currency.symbol}${it.amount} (${it.category.displayName})"
        }

        return """
            You are an enterprise risk and fraud detection AI.
            Analyze the following transactions for anomalies, duplicate billings, or unexpected spikes:
            $expenseSummary
            
            Respond in JSON array format:
            [
              {
                "id": "insight_1",
                "type": "ANOMALY_DETECTED",
                "severity": "WARNING",
                "title": "Anomaly Title",
                "message": "Detailed explanation",
                "confidenceScore": 0.95
              }
            ]
        """.trimIndent()
    }
}
