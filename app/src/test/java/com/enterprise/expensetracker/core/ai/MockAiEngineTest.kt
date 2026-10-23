package com.enterprise.expensetracker.core.ai

import com.enterprise.expensetracker.core.ai.mock.MockAiEngine
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockAiEngineTest {

    private lateinit var aiEngine: MockAiEngine

    @Before
    fun setUp() {
        aiEngine = MockAiEngine()
    }

    @Test
    fun `generateMonthlyForecast produces valid predictive figures`() = runTest {
        val expenses = listOf(
            Expense(title = "Groceries", amount = 2000.0, category = Category.GROCERIES_AND_FOOD, currency = Currency.INR),
            Expense(title = "Fuel", amount = 4500.0, category = Category.TRAVEL_AND_FUEL, currency = Currency.INR)
        )

        val result = aiEngine.generateMonthlyForecast(expenses, monthlyBudget = 20000.0)

        assertTrue(result.isSuccess)
        val forecast = result.getOrNull()
        assertNotNull(forecast)
        assertEquals(6500.0, forecast!!.currentMonthActual, 0.01)
        assertTrue(forecast.predictedEndOfMonthSpend > 6500.0)
        assertTrue(forecast.confidenceRate > 0.9f)
    }

    @Test
    fun `detectAnomalies flags high value transactions`() = runTest {
        val expenses = listOf(
            Expense(title = "Daily Milk", amount = 60.0, category = Category.GROCERIES_AND_FOOD),
            Expense(title = "Home Painting", amount = 15000.0, category = Category.HOME_MAINTENANCE)
        )

        val result = aiEngine.detectAnomalies(expenses)

        assertTrue(result.isSuccess)
        val insights = result.getOrNull()
        assertNotNull(insights)
        assertTrue(insights!!.any { it.type == com.enterprise.expensetracker.core.model.ai.InsightType.ANOMALY_DETECTED })
    }
}
