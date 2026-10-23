package com.enterprise.expensetracker.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.ai.AiKeyManager
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.core.database.MongoDbManager
import com.enterprise.expensetracker.core.model.Budget
import com.enterprise.expensetracker.core.model.BudgetPeriod
import com.enterprise.expensetracker.core.model.BudgetScope
import com.enterprise.expensetracker.domain.repository.BudgetRepository
import com.enterprise.expensetracker.domain.usecase.GetCurrentUserUseCase
import com.enterprise.expensetracker.domain.usecase.GetDynamicConfigUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import com.enterprise.expensetracker.domain.usecase.LogoutUseCase
import com.enterprise.expensetracker.domain.usecase.UpdateAvatarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getDynamicConfigUseCase: GetDynamicConfigUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val budgetRepository: BudgetRepository,
    private val aiKeyManager: AiKeyManager,
    private val mongoDbManager: MongoDbManager
) : BaseViewModel<SettingsState, SettingsEvent, SettingsEffect>(SettingsState()) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("household_budget_settings", Context.MODE_PRIVATE)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val gKey = aiKeyManager.getGeminiKey()
        val oKey = aiKeyManager.getOpenAiKey()
        val cachedBudget = prefs.getFloat("household_family_budget", -1f).toDouble()
        val cachedInsights = prefs.getBoolean("smart_insights_enabled", true)

        setState {
            copy(
                geminiApiKey = gKey,
                openAiApiKey = oKey,
                currentFamilyBudget = if (cachedBudget > 0) cachedBudget else currentFamilyBudget,
                isSmartInsightsEnabled = cachedInsights
            )
        }

        viewModelScope.launch {
            combine(
                getCurrentUserUseCase(),
                budgetRepository.observeAllBudgets()
            ) { user, allBudgets ->
                val familyId = user?.familyId?.ifBlank { "family_primary" } ?: "family_primary"
                val fb = allBudgets.firstOrNull { it.familyId == familyId && it.scope == BudgetScope.FAMILY }
                    ?: allBudgets.firstOrNull { it.scope == BudgetScope.FAMILY }
                Pair(user, fb)
            }.collect { (user, familyBudget) ->
                val limit = familyBudget?.monthlyAmount
                    ?: if (cachedBudget > 0) cachedBudget else 50000.0
                setState {
                    copy(
                        currentUser = user,
                        currentFamilyBudget = limit
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
                        sendEffect(SettingsEffect.ShowNotification("Preferences updated from cloud"))
                    } else {
                        sendEffect(SettingsEffect.ShowNotification("Using cached household preferences"))
                    }
                }
            }
            is SettingsEvent.SyncMongoAtlas -> {
                viewModelScope.launch {
                    val result = mongoDbManager.triggerSync()
                    if (result.isSuccess) {
                        sendEffect(SettingsEffect.ShowNotification("Synced successfully with cloud backup"))
                    } else {
                        sendEffect(SettingsEffect.ShowNotification("Cloud backup queued"))
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
                    sendEffect(SettingsEffect.ShowNotification("Expense ledger reset successfully"))
                }
            }
            is SettingsEvent.SignOut -> {
                viewModelScope.launch {
                    logoutUseCase()
                    sendEffect(SettingsEffect.ShowNotification("Signed out successfully"))
                }
            }
            is SettingsEvent.SetAvatarDialogVisible -> {
                setState { copy(isAvatarDialogOpen = event.isVisible) }
            }
            is SettingsEvent.UpdateAvatar -> {
                viewModelScope.launch {
                    val res = updateAvatarUseCase(event.avatarUrl)
                    if (res.isSuccess) {
                        setState { copy(isAvatarDialogOpen = false) }
                        sendEffect(SettingsEffect.ShowNotification("Profile avatar updated & synced to Atlas!"))
                    } else {
                        sendEffect(SettingsEffect.ShowNotification("Failed to update avatar: ${res.exceptionOrNull()?.message}"))
                    }
                }
            }
            is SettingsEvent.SetBudgetPrefDialogVisible -> {
                setState { copy(isBudgetPrefDialogOpen = event.isVisible) }
            }
            is SettingsEvent.SaveBudgetPreference -> {
                viewModelScope.launch {
                    val user = currentState.currentUser
                    val familyId = user?.familyId?.ifBlank { "family_primary" } ?: "family_primary"
                    val budget = Budget(
                        id = "${familyId}_FAMILY",
                        familyId = familyId,
                        userId = null,
                        userName = if (user?.isHeadOfHousehold == true) "Family Head" else (user?.name ?: "Family Head"),
                        scope = BudgetScope.FAMILY,
                        baseAmount = event.limit,
                        periodType = BudgetPeriod.MONTHLY,
                        updatedAt = System.currentTimeMillis()
                    )

                    prefs.edit()
                        .putFloat("household_family_budget", event.limit.toFloat())
                        .putBoolean("smart_insights_enabled", event.isSmartInsightsEnabled)
                        .apply()

                    val res = budgetRepository.saveBudget(budget)
                    if (res.isSuccess) {
                        setState {
                            copy(
                                isBudgetPrefDialogOpen = false,
                                currentFamilyBudget = event.limit,
                                isSmartInsightsEnabled = event.isSmartInsightsEnabled,
                                config = config.copy(
                                    maxSingleExpenseThreshold = event.limit,
                                    isAiInsightsEnabled = event.isSmartInsightsEnabled
                                )
                            )
                        }
                        sendEffect(SettingsEffect.ShowNotification("Household budget limit updated to ₹${event.limit.toInt()}!"))
                    } else {
                        sendEffect(SettingsEffect.ShowNotification("Failed to update budget: ${res.exceptionOrNull()?.message}"))
                    }
                }
            }
            is SettingsEvent.SaveAiKey -> {
                if (event.provider.equals("GEMINI", ignoreCase = true)) {
                    aiKeyManager.saveGeminiKey(event.apiKey)
                    setState { copy(geminiApiKey = event.apiKey) }
                    sendEffect(SettingsEffect.ShowNotification("Gemini API Key saved and active!"))
                } else if (event.provider.equals("OPENAI", ignoreCase = true)) {
                    aiKeyManager.saveOpenAiKey(event.apiKey)
                    setState { copy(openAiApiKey = event.apiKey) }
                    sendEffect(SettingsEffect.ShowNotification("OpenAI API Key saved and active!"))
                }
            }
            is SettingsEvent.TestAiConnection -> {
                viewModelScope.launch {
                    setState { copy(isTestingAi = true, aiTestResult = null) }
                    val result = aiKeyManager.testConnection(event.provider)
                    setState {
                        copy(
                            isTestingAi = false,
                            aiTestResult = if (result.isSuccess) result.getOrNull() else "Error: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            }
            is SettingsEvent.DismissAiTestResult -> {
                setState { copy(aiTestResult = null) }
            }
        }
    }
}
