package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.DynamicAppConfig
import com.enterprise.expensetracker.domain.repository.DynamicConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDynamicConfigUseCase @Inject constructor(
    private val dynamicConfigRepository: DynamicConfigRepository
) {
    fun observeConfig(): Flow<DynamicAppConfig> {
        return dynamicConfigRepository.observeConfig()
    }

    suspend fun refresh(): Result<Boolean> {
        return dynamicConfigRepository.refreshConfig()
    }
}
