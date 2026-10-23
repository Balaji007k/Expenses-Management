package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFamilyMembersUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<List<UserProfile>> {
        return authRepository.observeFamilyMembers()
    }
}

class AddFamilyMemberUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        relationship: FamilyRelationship
    ): Result<UserProfile> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Member name cannot be empty"))
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid member email"))
        }
        return authRepository.addFamilyMember(name, email, relationship)
    }
}

class RemoveFamilyMemberUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(memberId: String): Result<Unit> {
        return authRepository.removeFamilyMember(memberId)
    }
}

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, relationship: FamilyRelationship): Result<UserProfile> {
        return authRepository.updateProfile(name, relationship)
    }
}
