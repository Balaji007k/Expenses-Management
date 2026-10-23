package com.enterprise.expensetracker.feature.settings

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.database.MongoDbManager
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
    private val mongoDbManager: MongoDbManager
) : BaseViewModel<SettingsState, SettingsEvent, SettingsEffect>(SettingsState()) {

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getDynamicConfigUseCase.observeConfig().collect { config ->
                setState {
                    copy(
                        config = config,
                        activeAiProvider = config.activeAiProvider
                    )
                }
            }
        }

        viewModelScope.launch {
            getExpensesUseCase().collect { list ->
                setState { copy(totalLocalExpensesCount = list.size) }
            }
        }

        viewModelScope.launch {
            mongoDbManager.connectionState.collect { connState ->
                setState { copy(mongoStatus = connState.name) }
            }
        }
    }

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.RefreshFirebaseConfig -> {
                viewModelScope.launch {
                    setState { copy(isRefreshingConfig = true) }
                    val result = getDynamicConfigUseCase.refresh()
                    setState { copy(isRefreshingConfig = false) }
                    if (result.isSuccess) {
                        sendEffect(SettingsEffect.ShowNotification("Firebase Remote Config activated"))
                    } else {
                        sendEffect(SettingsEffect.ShowNotification("Using enterprise cached config"))
                    }
                }
            }
            is SettingsEvent.SyncMongoAtlas -> {
                viewModelScope.launch {
                    val result = mongoDbManager.triggerSync()
                    if (result.isSuccess) {
                        sendEffect(SettingsEffect.ShowNotification("Synced successfully with MongoDB Atlas"))
                    } else {
                        sendEffect(SettingsEffect.ShowNotification("MongoDB sync queued"))
                    }
                }
            }
            is SettingsEvent.SwitchAiProvider -> {
                setState {
                    copy(
                        activeAiProvider = event.provider,
                        config = config.copy(activeAiProvider = event.provider)
                    )
                }
                sendEffect(SettingsEffect.ShowNotification("AI Provider switched to ${event.provider}"))
            }
            is SettingsEvent.ClearLocalDatabase -> {
                viewModelScope.launch {
                    mongoDbManager.expenseDao.clearAll()
                    sendEffect(SettingsEffect.ShowNotification("Local cache cleared"))
                }
            }
        }
    }
}
