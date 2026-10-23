package com.enterprise.expensetracker.feature.insights

import com.enterprise.expensetracker.core.model.ReportPeriod
import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.SpendingForecast
import com.enterprise.expensetracker.domain.usecase.RefinedAnalyticsReport

enum class ReportTab {
    ITEM_CONSUMPTION,
    PRICE_VARIATION,
    CATEGORY_DISTRIBUTION,
    AI_ADVISOR
}

data class InsightsState(
    val isLoading: Boolean = false,
    val selectedPeriod: ReportPeriod = ReportPeriod.MONTH,
    val selectedTab: ReportTab = ReportTab.ITEM_CONSUMPTION,
    val analyticsReport: RefinedAnalyticsReport? = null,
    val forecast: SpendingForecast? = null,
    val insights: List<AiInsight> = emptyList(),
    val activeAiProvider: String = "Gemini",
    val errorMessage: String? = null
)

sealed interface InsightsEvent {
    data class SelectPeriod(val period: ReportPeriod) : InsightsEvent
    data class SelectTab(val tab: ReportTab) : InsightsEvent
    data object RefreshInsights : InsightsEvent
}

sealed interface InsightsEffect {
    data class ShowToast(val message: String) : InsightsEffect
}
