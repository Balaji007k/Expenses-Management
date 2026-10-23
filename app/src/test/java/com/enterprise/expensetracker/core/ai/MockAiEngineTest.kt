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
            Expense(title = "Cloud Server", amount = 200.0, category = Category.SOFTWARE_AND_TECH, currency = Currency.USD),
            Expense(title = "Client Flight", amount = 450.0, category = Category.TRAVEL_AND_TRANSPORT, currency = Currency.USD)
        )

        val result = aiEngine.generateMonthlyForecast(expenses, monthlyBudget = 2000.0)

        assertTrue(result.isSuccess)
        val forecast = result.getOrNull()
        assertNotNull(forecast)
        assertEquals(650.0, forecast!!.currentMonthActual, 0.01)
        assertTrue(forecast.predictedEndOfMonthSpend > 650.0)
        assertTrue(forecast.confidenceRate > 0.9f)
    }

    @Test
    fun `detectAnomalies flags high value transactions`() = runTest {
        val expenses = listOf(
            Expense(title = "Office Supplies", amount = 25.0, category = Category.OFFICE_AND_EQUIPMENT),
            Expense(title = "Executive Retreat", amount = 850.0, category = Category.TRAVEL_AND_TRANSPORT)
        )

        val result = aiEngine.detectAnomalies(expenses)

        assertTrue(result.isSuccess)
        val insights = result.getOrNull()
        assertNotNull(insights)
        assertTrue(insights!!.any { it.title.contains("Unusual") || it.title.contains("Peak") })
    }
}
