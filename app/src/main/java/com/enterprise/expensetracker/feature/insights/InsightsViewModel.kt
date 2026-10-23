package com.enterprise.expensetracker.feature.insights

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.common.result.Resource
import com.enterprise.expensetracker.domain.usecase.GetAiInsightsUseCase
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getAiInsightsUseCase: GetAiInsightsUseCase,
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase
) : BaseViewModel<InsightsState, InsightsEvent, InsightsEffect>(InsightsState()) {

    init {
        loadAiData()
    }

    private fun loadAiData() {
        setState { copy(isLoading = true) }

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
                        isLoading = false,
                        forecast = forecast,
                        insights = anomalies
                    )
                }
            }
        }
    }

    override fun handleEvent(event: InsightsEvent) {
        when (event) {
            is InsightsEvent.RefreshInsights -> {
                loadAiData()
                sendEffect(InsightsEffect.ShowToast("Refreshed AI predictions"))
            }
        }
    }
}
