package com.enterprise.expensetracker.feature.insights

import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.SpendingForecast

data class InsightsState(
    val isLoading: Boolean = false,
    val forecast: SpendingForecast? = null,
    val insights: List<AiInsight> = emptyList(),
    val activeAiProvider: String = "Gemini",
    val errorMessage: String? = null
)

sealed interface InsightsEvent {
    data object RefreshInsights : InsightsEvent
}

sealed interface InsightsEffect {
    data class ShowToast(val message: String) : InsightsEffect
}
