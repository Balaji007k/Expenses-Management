package com.enterprise.expensetracker.feature.settings

import com.enterprise.expensetracker.core.model.DynamicAppConfig

data class SettingsState(
    val config: DynamicAppConfig = DynamicAppConfig(),
    val mongoStatus: String = "Connected",
    val activeAiProvider: String = "GEMINI",
    val isRefreshingConfig: Boolean = false,
    val totalLocalExpensesCount: Int = 0
)

sealed interface SettingsEvent {
    data object RefreshFirebaseConfig : SettingsEvent
    data class SwitchAiProvider(val provider: String) : SettingsEvent
    data object ClearLocalDatabase : SettingsEvent
}

sealed interface SettingsEffect {
    data class ShowNotification(val message: String) : SettingsEffect
}
