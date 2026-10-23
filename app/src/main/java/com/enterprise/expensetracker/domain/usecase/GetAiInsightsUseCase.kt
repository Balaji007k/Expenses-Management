package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.SpendingForecast
import com.enterprise.expensetracker.domain.repository.AiInsightsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAiInsightsUseCase @Inject constructor(
    private val aiInsightsRepository: AiInsightsRepository
) {
    fun getForecast(monthlyBudget: Double = 5000.0): Flow<Resource<SpendingForecast>> {
        return aiInsightsRepository.getSpendingForecast(monthlyBudget)
    }

    fun getAnomalies(): Flow<Resource<List<AiInsight>>> {
        return aiInsightsRepository.getAnomalies()
    }
}
