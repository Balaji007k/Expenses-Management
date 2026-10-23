package com.enterprise.expensetracker.domain.repository

import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.SpendingForecast
import kotlinx.coroutines.flow.Flow

interface AiInsightsRepository {
    fun getSpendingForecast(monthlyBudget: Double): Flow<Resource<SpendingForecast>>
    fun getAnomalies(): Flow<Resource<List<AiInsight>>>
    suspend fun suggestCategory(title: String, amount: Double): Result<Category>
}
