package com.enterprise.expensetracker.domain.repository

import com.enterprise.expensetracker.core.model.DynamicAppConfig
import kotlinx.coroutines.flow.Flow

interface DynamicConfigRepository {
    fun observeConfig(): Flow<DynamicAppConfig>
    suspend fun refreshConfig(): Result<Boolean>
}
