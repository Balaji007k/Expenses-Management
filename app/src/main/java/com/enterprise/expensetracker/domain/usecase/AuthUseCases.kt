package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole
import com.enterprise.expensetracker.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserProfile> {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return authRepository.loginWithEmail(email, password)
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        relationship: FamilyRelationship
    ): Result<UserProfile> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your full name"))
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return authRepository.registerWithEmail(name, email, password, role, relationship)
    }
}

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(displayName: String?, email: String?): Result<UserProfile> {
        return authRepository.loginWithGoogle(displayName, email)
    }
}

class DemoLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(role: UserRole): Result<UserProfile> {
        return authRepository.loginAsDemo(role)
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
