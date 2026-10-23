package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Currency
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddExpenseUseCaseTest {

    private val repository: ExpenseRepository = mockk(relaxed = true)
    private lateinit var useCase: AddExpenseUseCase

    @Before
    fun setUp() {
        useCase = AddExpenseUseCase(repository)
    }

    @Test
    fun `when adding valid expense, repository should receive call and succeed`() = runTest {
        val expense = Expense(
            title = "AWS Infrastructure",
            amount = 499.0,
            category = Category.SOFTWARE_AND_TECH,
            currency = Currency.USD
        )
        coEvery { repository.addExpense(expense) } returns Result.success(Unit)

        val result = useCase(expense)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.addExpense(expense) }
    }

    @Test
    fun `when title is blank, should fail with validation error`() = runTest {
        val expense = Expense(
            title = "",
            amount = 100.0,
            category = Category.MISCELLANEOUS
        )

        val result = useCase(expense)

        assertTrue(result.isFailure)
        assertEquals("Expense title cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `when amount is zero or negative, should fail with validation error`() = runTest {
        val expense = Expense(
            title = "Lunch",
            amount = 0.0,
            category = Category.FOOD_AND_DINING
        )

        val result = useCase(expense)

        assertTrue(result.isFailure)
        assertEquals("Expense amount must be greater than zero", result.exceptionOrNull()?.message)
    }
}
