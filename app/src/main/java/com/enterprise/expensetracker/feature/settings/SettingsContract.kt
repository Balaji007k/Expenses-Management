package com.enterprise.expensetracker.feature.settings

import com.enterprise.expensetracker.core.model.DynamicAppConfig
import com.enterprise.expensetracker.core.model.UserProfile

data class SettingsState(
    val config: DynamicAppConfig = DynamicAppConfig(),
    val currentUser: UserProfile? = null,
    val mongoStatus: String = "Connected",
    val activeAiProvider: String = "GEMINI",
    val isRefreshingConfig: Boolean = false,
    val isAvatarDialogOpen: Boolean = false,
    val isBudgetPrefDialogOpen: Boolean = false,
    val currentFamilyBudget: Double = 50000.0,
    val isSmartInsightsEnabled: Boolean = true,
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val isTestingAi: Boolean = false,
    val aiTestResult: String? = null,
    val totalLocalExpensesCount: Int = 0
)

sealed interface SettingsEvent {
    data object RefreshFirebaseConfig : SettingsEvent
    data object SyncMongoAtlas : SettingsEvent
    data class SwitchAiProvider(val provider: String) : SettingsEvent
    data object ClearLocalDatabase : SettingsEvent
    data object SignOut : SettingsEvent
    data class SetAvatarDialogVisible(val isVisible: Boolean) : SettingsEvent
    data class UpdateAvatar(val avatarUrl: String) : SettingsEvent
    data class SetBudgetPrefDialogVisible(val isVisible: Boolean) : SettingsEvent
    data class SaveBudgetPreference(val limit: Double, val isSmartInsightsEnabled: Boolean) : SettingsEvent
    data class SaveAiKey(val provider: String, val apiKey: String) : SettingsEvent
    data class TestAiConnection(val provider: String) : SettingsEvent
    data object DismissAiTestResult : SettingsEvent
}

sealed interface SettingsEffect {
    data class ShowNotification(val message: String) : SettingsEffect
}
