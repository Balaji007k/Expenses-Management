package com.enterprise.expensetracker.feature.settings

import com.enterprise.expensetracker.core.model.DynamicAppConfig
import com.enterprise.expensetracker.core.model.UserProfile

data class SettingsState(
    val config: DynamicAppConfig = DynamicAppConfig(),
    val currentUser: UserProfile? = null,
    val mongoStatus: String = "Connected",
    val activeAiProvider: String = "GEMINI",
    val isRefreshingConfig: Boolean = false,
    val totalLocalExpensesCount: Int = 0
)

sealed interface SettingsEvent {
    data object RefreshFirebaseConfig : SettingsEvent
    data object SyncMongoAtlas : SettingsEvent
    data class SwitchAiProvider(val provider: String) : SettingsEvent
    data object ClearLocalDatabase : SettingsEvent
    data object SignOut : SettingsEvent
}

sealed interface SettingsEffect {
    data class ShowNotification(val message: String) : SettingsEffect
}
