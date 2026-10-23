package com.enterprise.expensetracker.data.repository

import com.enterprise.expensetracker.core.model.DynamicAppConfig
import com.enterprise.expensetracker.core.remoteconfig.FirebaseConfigManager
import com.enterprise.expensetracker.domain.repository.DynamicConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicConfigRepositoryImpl @Inject constructor(
    private val firebaseConfigManager: FirebaseConfigManager
) : DynamicConfigRepository {

    override fun observeConfig(): Flow<DynamicAppConfig> {
        return firebaseConfigManager.dynamicConfig
    }

    override suspend fun refreshConfig(): Result<Boolean> {
        return firebaseConfigManager.fetchAndActivate()
    }
}
