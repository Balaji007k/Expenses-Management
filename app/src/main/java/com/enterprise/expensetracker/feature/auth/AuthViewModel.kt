package com.enterprise.expensetracker.feature.auth

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.domain.usecase.GoogleLoginUseCase
import com.enterprise.expensetracker.domain.usecase.LoginUseCase
import com.enterprise.expensetracker.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase
) : BaseViewModel<AuthState, AuthEvent, AuthEffect>(AuthState()) {

    override fun handleEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.NameChanged -> setState { copy(name = event.name, errorMessage = null) }
            is AuthEvent.EmailChanged -> setState { copy(email = event.email, errorMessage = null) }
            is AuthEvent.PasswordChanged -> setState { copy(password = event.password, errorMessage = null) }
            is AuthEvent.TogglePasswordVisibility -> setState { copy(isPasswordVisible = !isPasswordVisible) }
            is AuthEvent.RoleChanged -> setState { copy(selectedRole = event.role) }
            is AuthEvent.RelationshipChanged -> setState { copy(selectedRelationship = event.relationship) }
            is AuthEvent.ToggleMode -> setState {
                copy(
                    isRegisterMode = !isRegisterMode,
                    errorMessage = null
                )
            }
            is AuthEvent.DismissError -> setState { copy(errorMessage = null) }
            is AuthEvent.SubmitAuth -> handleSubmit()
            is AuthEvent.GoogleSignInClicked -> handleGoogleSignIn()
        }
    }

    private fun handleSubmit() {
        val state = currentState
        setState { copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            if (state.isRegisterMode) {
                val result = registerUseCase(
                    name = state.name,
                    email = state.email,
                    password = state.password,
                    role = state.selectedRole,
                    relationship = state.selectedRelationship
                )
                setState { copy(isLoading = false) }
                if (result.isSuccess) {
                    sendEffect(AuthEffect.ShowToast("Welcome, ${result.getOrNull()?.name}! Account created."))
                    sendEffect(AuthEffect.NavigateToHome)
                } else {
                    setState { copy(errorMessage = result.exceptionOrNull()?.message ?: "Registration failed") }
                }
            } else {
                val result = loginUseCase(
                    email = state.email,
                    password = state.password
                )
                setState { copy(isLoading = false) }
                if (result.isSuccess) {
                    sendEffect(AuthEffect.ShowToast("Welcome back, ${result.getOrNull()?.name}!"))
                    sendEffect(AuthEffect.NavigateToHome)
                } else {
                    setState { copy(errorMessage = result.exceptionOrNull()?.message ?: "Login failed") }
                }
            }
        }
    }

    private fun handleGoogleSignIn() {
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = googleLoginUseCase(
                displayName = if (currentState.name.isNotBlank()) currentState.name else "Household User",
                email = if (currentState.email.isNotBlank()) currentState.email else "google.user@household.in"
            )
            setState { copy(isLoading = false) }
            if (result.isSuccess) {
                sendEffect(AuthEffect.ShowToast("Google Sign-In successful!"))
                sendEffect(AuthEffect.NavigateToHome)
            } else {
                setState { copy(errorMessage = result.exceptionOrNull()?.message ?: "Google Sign-In failed") }
            }
        }
    }
}
