package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<UserProfile?> {
        return authRepository.observeCurrentUser()
    }

    suspend fun getSync(): UserProfile? {
        return authRepository.getCurrentUser()
    }
}
