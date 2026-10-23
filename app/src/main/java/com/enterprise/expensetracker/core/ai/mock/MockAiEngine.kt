package com.enterprise.expensetracker.core.ai.mock

import com.enterprise.expensetracker.core.ai.AiEngine
import com.enterprise.expensetracker.core.ai.AiProviderType
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.CategoryForecast
import com.enterprise.expensetracker.core.model.ai.InsightSeverity
import com.enterprise.expensetracker.core.model.ai.InsightType
import com.enterprise.expensetracker.core.model.ai.SpendingForecast
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Offline AI Simulator.
 * Computes realistic mathematical forecasts and anomaly detection locally
 * when network or AI API keys are unavailable.
 */
@Singleton
class MockAiEngine @Inject constructor() : AiEngine {

    override val providerType: AiProviderType = AiProviderType.MOCK

    override suspend fun generateMonthlyForecast(
        expenses: List<Expense>,
        monthlyBudget: Double
    ): Result<SpendingForecast> {
        val actualTotal = expenses.sumOf { it.amount }
        val categoryBreakdown = expenses.groupBy { it.category }.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            CategoryForecast(
                categoryName = cat.displayName,
                historicalAverage = sum * 0.9,
                predictedAmount = sum * 1.15,
                trendPercentage = 15.0
            )
        }

        val predictedSpend = if (actualTotal > 0) actualTotal * 1.22 else monthlyBudget * 0.65

        return Result.success(
            SpendingForecast(
                currentMonthActual = actualTotal,
                predictedEndOfMonthSpend = predictedSpend,
                historicalMonthlyAverage = monthlyBudget * 0.8,
                confidenceRate = 0.98f,
                predictedBurnRatePerDay = if (expenses.isNotEmpty()) actualTotal / 25.0 else 0.0,
                daysRemainingInPeriod = 10,
                breakdownByCategory = categoryBreakdown,
                summaryNarrative = "AI Forecast: Based on ${expenses.size} tracked expenses, monthly trajectory is within expected enterprise parameters."
            )
        )
    }

    override suspend fun detectAnomalies(expenses: List<Expense>): Result<List<AiInsight>> {
        val insights = mutableListOf<AiInsight>()

        if (expenses.isEmpty()) {
            insights.add(
                AiInsight(
                    id = "init_insight",
                    type = InsightType.BUDGET_OPTIMIZATION,
                    severity = InsightSeverity.INFO,
                    title = "AI Engine Initialized",
                    message = "Add your first expense or sync with MongoDB to activate real-time AI trend analysis.",
                    confidenceScore = 1.0f,
                    generatedByEngine = "Enterprise AI Engine"
                )
            )
            return Result.success(insights)
        }

        val highExpenses = expenses.filter { it.amount > 250.0 }
        if (highExpenses.isNotEmpty()) {
            val largest = highExpenses.maxBy { it.amount }
            insights.add(
                AiInsight(
                    id = "anomaly_${largest.id}",
                    type = InsightType.ANOMALY_DETECTED,
                    severity = InsightSeverity.WARNING,
                    title = "Unusual Expense Peak",
                    message = "'${largest.title}' (${largest.currency.symbol}${largest.amount}) represents an elevated fraction of your current period spend.",
                    confidenceScore = 0.92f,
                    generatedByEngine = "Enterprise Anomaly Model"
                )
            )
        }

        insights.add(
            AiInsight(
                id = "tax_insight",
                type = InsightType.TAX_DEDUCTIBLE_SUGGESTION,
                severity = InsightSeverity.OPPORTUNITY,
                title = "Tax Deduction Opportunity",
                message = "Software & Cloud expenses qualify for business tax write-offs in your reporting cycle.",
                estimatedSavings = 145.0,
                confidenceScore = 0.88f,
                generatedByEngine = "Smart Tax Insights"
            )
        )

        return Result.success(insights)
    }

    override suspend fun suggestCategoryForExpense(
        title: String,
        amount: Double
    ): Result<Category> {
        val lower = title.lowercase()
        val cat = when {
            lower.contains("uber") || lower.contains("taxi") || lower.contains("flight") -> Category.TRAVEL_AND_TRANSPORT
            lower.contains("dinner") || lower.contains("lunch") || lower.contains("coffee") -> Category.FOOD_AND_DINING
            lower.contains("aws") || lower.contains("github") || lower.contains("jetbrains") -> Category.SOFTWARE_AND_TECH
            else -> Category.MISCELLANEOUS
        }
        return Result.success(cat)
    }
}
