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
 * Intelligent Home Expense AI Advisor Simulator.
 * Computes statistical household forecasts and savings suggestions.
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

        val predictedSpend = if (actualTotal > 0) actualTotal * 1.2 else monthlyBudget * 0.7

        return Result.success(
            SpendingForecast(
                currentMonthActual = actualTotal,
                predictedEndOfMonthSpend = predictedSpend,
                historicalMonthlyAverage = monthlyBudget * 0.85,
                confidenceRate = 0.95f,
                predictedBurnRatePerDay = if (expenses.isNotEmpty()) actualTotal / 25.0 else 0.0,
                daysRemainingInPeriod = 10,
                breakdownByCategory = categoryBreakdown,
                summaryNarrative = "AI Advisor: Based on ${expenses.size} home expenses, your household spending is comfortably within the monthly target."
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
                    title = "Smart Advisor Ready",
                    message = "Add your daily groceries, electricity, or utility bills to start smart budget tracking.",
                    confidenceScore = 1.0f,
                    generatedByEngine = "Home AI Advisor"
                )
            )
            return Result.success(insights)
        }

        val highExpenses = expenses.filter { it.amount > 2500.0 }
        if (highExpenses.isNotEmpty()) {
            val largest = highExpenses.maxBy { it.amount }
            insights.add(
                AiInsight(
                    id = "anomaly_${largest.id}",
                    type = InsightType.ANOMALY_DETECTED,
                    severity = InsightSeverity.WARNING,
                    title = "High Household Expense",
                    message = "'${largest.title}' (${largest.currency.symbol}${largest.amount.toInt()}) is a major contributor to this month's budget.",
                    confidenceScore = 0.92f,
                    generatedByEngine = "Budget Advisor"
                )
            )
        }

        insights.add(
            AiInsight(
                id = "grocery_tip",
                type = InsightType.BUDGET_OPTIMIZATION,
                severity = InsightSeverity.OPPORTUNITY,
                title = "Grocery & Utility Saving Tip",
                message = "Switching to monthly pantry bulk orders and smart power saver habits can reduce bills.",
                estimatedSavings = 1500.0,
                confidenceScore = 0.89f,
                generatedByEngine = "Family Budget Advisor"
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
            lower.contains("milk") || lower.contains("grocery") || lower.contains("vegetable") || lower.contains("food") -> Category.GROCERIES_AND_FOOD
            lower.contains("rent") || lower.contains("mortgage") || lower.contains("flat") -> Category.RENT_AND_HOUSING
            lower.contains("electric") || lower.contains("water") || lower.contains("gas") || lower.contains("bill") -> Category.UTILITIES_AND_BILLS
            lower.contains("wifi") || lower.contains("internet") || lower.contains("recharge") -> Category.INTERNET_AND_MOBILE
            lower.contains("school") || lower.contains("tuition") || lower.contains("book") -> Category.KIDS_AND_EDUCATION
            lower.contains("doctor") || lower.contains("pharmacy") || lower.contains("medicine") -> Category.HEALTH_AND_MEDICINE
            lower.contains("repair") || lower.contains("plumb") || lower.contains("maid") -> Category.HOME_MAINTENANCE
            lower.contains("clothes") || lower.contains("dress") || lower.contains("shopping") -> Category.SHOPPING_AND_CLOTHING
            lower.contains("fuel") || lower.contains("petrol") || lower.contains("bus") -> Category.TRAVEL_AND_FUEL
            lower.contains("dinner") || lower.contains("treat") || lower.contains("snack") -> Category.DINING_AND_OUTING
            else -> Category.MISCELLANEOUS
        }
        return Result.success(cat)
    }
}
