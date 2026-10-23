package com.enterprise.expensetracker.feature.auth

import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserRole

data class AuthState(
    val isLoading: Boolean = false,
    val isRegisterMode: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val selectedRole: UserRole = UserRole.HEAD_OF_HOUSEHOLD,
    val selectedRelationship: FamilyRelationship = FamilyRelationship.HEAD,
    val errorMessage: String? = null
)

sealed interface AuthEvent {
    data class NameChanged(val name: String) : AuthEvent
    data class EmailChanged(val email: String) : AuthEvent
    data class PasswordChanged(val password: String) : AuthEvent
    data object TogglePasswordVisibility : AuthEvent
    data class RoleChanged(val role: UserRole) : AuthEvent
    data class RelationshipChanged(val relationship: FamilyRelationship) : AuthEvent
    data object ToggleMode : AuthEvent
    data object SubmitAuth : AuthEvent
    data object GoogleSignInClicked : AuthEvent
    data class DemoLoginClicked(val role: UserRole) : AuthEvent
    data object DismissError : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data class ShowToast(val message: String) : AuthEffect
}
