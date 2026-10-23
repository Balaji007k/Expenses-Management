package com.enterprise.expensetracker.core.remoteconfig

import com.enterprise.expensetracker.core.model.DynamicAppConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * Enterprise Firebase Remote Configuration abstraction.
 * Supplies dynamic operational parameters, feature toggles, and AI provider routing.
 */
interface FirebaseConfigManager {
    val dynamicConfig: StateFlow<DynamicAppConfig>

    suspend fun fetchAndActivate(): Result<Boolean>
    fun getString(key: String, defaultValue: String): String
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getDouble(key: String, defaultValue: Double): Double
    fun getLong(key: String, defaultValue: Long): Long
}
