package com.enterprise.expensetracker.feature.insights

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.core.model.ReportPeriod
import com.enterprise.expensetracker.domain.usecase.GetAiInsightsUseCase
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import com.enterprise.expensetracker.domain.usecase.GetRefinedAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getRefinedAnalyticsUseCase: GetRefinedAnalyticsUseCase,
    private val getAiInsightsUseCase: GetAiInsightsUseCase,
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase
) : BaseViewModel<InsightsState, InsightsEvent, InsightsEffect>(InsightsState()) {

    private var analyticsJob: Job? = null

    init {
        loadData(currentState.selectedPeriod)
    }

    private fun loadData(period: ReportPeriod) {
        setState { copy(isLoading = true, selectedPeriod = period) }

        // 1. Observe Refined Analytics for selected period
        analyticsJob?.cancel()
        analyticsJob = viewModelScope.launch {
            getRefinedAnalyticsUseCase(period).collect { report ->
                setState {
                    copy(
                        isLoading = false,
                        analyticsReport = report
                    )
                }
            }
        }

        // 2. Observe AI configuration & insights
        viewModelScope.launch {
            getDynamicConfigUseCase.observeConfig().collect { config ->
                setState { copy(activeAiProvider = config.activeAiProvider) }
            }
        }

        viewModelScope.launch {
            combine(
                getAiInsightsUseCase.getForecast(),
                getAiInsightsUseCase.getAnomalies()
            ) { forecastRes, anomaliesRes ->
                Pair(forecastRes, anomaliesRes)
            }.collect { (forecastRes, anomaliesRes) ->
                val forecast = (forecastRes as? Resource.Success)?.data
                val anomalies = (anomaliesRes as? Resource.Success)?.data ?: emptyList()

                setState {
                    copy(
                        forecast = forecast,
                        insights = anomalies
                    )
                }
            }
        }
    }

    override fun handleEvent(event: InsightsEvent) {
        when (event) {
            is InsightsEvent.SelectPeriod -> {
                loadData(event.period)
            }
            is InsightsEvent.SelectTab -> {
                setState { copy(selectedTab = event.tab) }
            }
            is InsightsEvent.RefreshInsights -> {
                loadData(currentState.selectedPeriod)
                sendEffect(InsightsEffect.ShowToast("Refreshed analytics and insights"))
            }
        }
    }
}
