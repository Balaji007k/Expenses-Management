package com.enterprise.expensetracker.data.repository

import com.enterprise.expensetracker.core.ai.AiEngineFactory
import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.SpendingForecast
import com.enterprise.expensetracker.domain.repository.AiInsightsRepository
import com.enterprise.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiInsightsRepositoryImpl @Inject constructor(
    private val aiEngineFactory: AiEngineFactory,
    private val expenseRepository: ExpenseRepository,
    private val dispatchers: CoroutineDispatchers
) : AiInsightsRepository {

    override fun getSpendingForecast(monthlyBudget: Double): Flow<Resource<SpendingForecast>> = flow {
        emit(Resource.Loading)
        try {
            val expenses = expenseRepository.observeAllExpenses().firstOrNull() ?: emptyList()
            val engine = aiEngineFactory.getActiveEngine()
            val result = engine.generateMonthlyForecast(expenses, monthlyBudget)
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrThrow()))
            } else {
                emit(Resource.Error(result.exceptionOrNull()?.localizedMessage ?: "Failed to generate AI forecast"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "AI Forecast computation failed", e))
        }
    }.flowOn(dispatchers.io)

    override fun getAnomalies(): Flow<Resource<List<AiInsight>>> = flow {
        emit(Resource.Loading)
        try {
            val expenses = expenseRepository.observeAllExpenses().firstOrNull() ?: emptyList()
            val engine = aiEngineFactory.getActiveEngine()
            val result = engine.detectAnomalies(expenses)
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrThrow()))
            } else {
                emit(Resource.Error(result.exceptionOrNull()?.localizedMessage ?: "Failed to detect anomalies"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "AI Anomaly detection failed", e))
        }
    }.flowOn(dispatchers.io)

    override suspend fun suggestCategory(title: String, amount: Double): Result<Category> {
        return withContext(dispatchers.io) {
            aiEngineFactory.getActiveEngine().suggestCategoryForExpense(title, amount)
        }
    }
}
