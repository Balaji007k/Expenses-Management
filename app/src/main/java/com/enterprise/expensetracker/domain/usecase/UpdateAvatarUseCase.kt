package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(avatarUrl: String): Result<UserProfile> {
        return authRepository.updateAvatar(avatarUrl)
    }
}
