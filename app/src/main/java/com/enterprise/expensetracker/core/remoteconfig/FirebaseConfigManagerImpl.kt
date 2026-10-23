package com.enterprise.expensetracker.core.remoteconfig

import android.content.Context
import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.model.DynamicAppConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of FirebaseConfigManager.
 * Loads remote parameters from Firebase with fallback to offline enterprise defaults.
 */
@Singleton
class FirebaseConfigManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers
) : FirebaseConfigManager {

    private val _dynamicConfig = MutableStateFlow(DynamicAppConfig())
    override val dynamicConfig: StateFlow<DynamicAppConfig> = _dynamicConfig.asStateFlow()

    private var remoteConfig: FirebaseRemoteConfig? = null

    init {
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                remoteConfig = FirebaseRemoteConfig.getInstance().apply {
                    val configSettings = FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(0)
                        .build()
                    setConfigSettingsAsync(configSettings)
                    setDefaultsAsync(getDefaultsMap())
                }
            }
        } catch (e: Exception) {
            // Firebase not initialized yet or running in offline mock environment
        }
    }

    private fun getDefaultsMap(): Map<String, Any> {
        return mapOf(
            ConfigKeys.KEY_ACTIVE_AI_PROVIDER to "GEMINI",
            ConfigKeys.KEY_ENABLE_AI_INSIGHTS to true,
            ConfigKeys.KEY_ENABLE_ANOMALY_DETECTION to true,
            ConfigKeys.KEY_DEFAULT_CURRENCY to "INR",
            ConfigKeys.KEY_MAX_SINGLE_EXPENSE_LIMIT to 50000.0,
            ConfigKeys.KEY_SYNC_INTERVAL_MINUTES to 15L,
            ConfigKeys.KEY_MONGO_REGION to "us-east-1",
            ConfigKeys.KEY_RECEIPT_SCAN_ENABLED to true,
            ConfigKeys.KEY_WELCOME_MESSAGE to "Welcome to Home Expense Tracker",
            ConfigKeys.KEY_DB_CONNECTION to "mongodb+srv://teabuff_db_user:1XPvy4K8hHmzZqfg@cluster0.fithesf.mongodb.net/ExpensesManagement"
        )
    }

    override suspend fun fetchAndActivate(): Result<Boolean> {
        return withContext(dispatchers.io) {
            try {
                val rc = remoteConfig
                if (rc != null) {
                    val activated = rc.fetchAndActivate().await()
                    updateConfigState()
                    Result.success(activated)
                } else {
                    Result.success(false)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun updateConfigState() {
        val rc = remoteConfig ?: return
        _dynamicConfig.value = DynamicAppConfig(
            activeAiProvider = rc.getString(ConfigKeys.KEY_ACTIVE_AI_PROVIDER).ifEmpty { "GEMINI" },
            isAiInsightsEnabled = rc.getBoolean(ConfigKeys.KEY_ENABLE_AI_INSIGHTS),
            isAnomalyDetectionEnabled = rc.getBoolean(ConfigKeys.KEY_ENABLE_ANOMALY_DETECTION),
            defaultCurrencyCode = rc.getString(ConfigKeys.KEY_DEFAULT_CURRENCY).ifEmpty { "INR" },
            maxSingleExpenseThreshold = rc.getDouble(ConfigKeys.KEY_MAX_SINGLE_EXPENSE_LIMIT).let { if (it <= 0) 50000.0 else it },
            syncIntervalMinutes = rc.getLong(ConfigKeys.KEY_SYNC_INTERVAL_MINUTES).let { if (it <= 0) 15L else it },
            mongoClusterRegion = rc.getString(ConfigKeys.KEY_MONGO_REGION).ifEmpty { "us-east-1" },
            isReceiptScanEnabled = rc.getBoolean(ConfigKeys.KEY_RECEIPT_SCAN_ENABLED),
            welcomeMessage = rc.getString(ConfigKeys.KEY_WELCOME_MESSAGE).ifEmpty { "Welcome to Home Expense Tracker" },
            dbConnectionUri = rc.getString(ConfigKeys.KEY_DB_CONNECTION).ifEmpty { "mongodb+srv://teabuff_db_user:1XPvy4K8hHmzZqfg@cluster0.fithesf.mongodb.net/ExpensesManagement" }
        )
    }

    override fun getString(key: String, defaultValue: String): String {
        return remoteConfig?.getString(key)?.takeIf { it.isNotEmpty() } ?: defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return remoteConfig?.getBoolean(key) ?: defaultValue
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        val value = remoteConfig?.getDouble(key) ?: 0.0
        return if (value != 0.0) value else defaultValue
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        val value = remoteConfig?.getLong(key) ?: 0L
        return if (value != 0L) value else defaultValue
    }
}
