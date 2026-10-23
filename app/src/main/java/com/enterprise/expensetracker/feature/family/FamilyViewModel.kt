package com.enterprise.expensetracker.feature.family

import androidx.lifecycle.viewModelScope
import com.enterprise.expensetracker.core.common.base.BaseViewModel
import com.enterprise.expensetracker.domain.usecase.AddFamilyMemberUseCase
import com.enterprise.expensetracker.domain.usecase.GetCurrentUserUseCase
import com.enterprise.expensetracker.domain.usecase.GetExpensesUseCase
import com.enterprise.expensetracker.domain.usecase.GetFamilyMembersUseCase
import com.enterprise.expensetracker.domain.usecase.LogoutUseCase
import com.enterprise.expensetracker.domain.usecase.RemoveFamilyMemberUseCase
import com.enterprise.expensetracker.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
    private val addFamilyMemberUseCase: AddFamilyMemberUseCase,
    private val removeFamilyMemberUseCase: RemoveFamilyMemberUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<FamilyState, FamilyEvent, FamilyEffect>(FamilyState()) {

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getCurrentUserUseCase(),
                getFamilyMembersUseCase(),
                getExpensesUseCase()
            ) { currentUser, members, expenses ->
                val counts = members.associate { member ->
                    member.id to expenses.count { exp ->
                        exp.createdByUserId == member.id || exp.createdByUserName.equals(member.name, ignoreCase = true)
                    }
                }
                Triple(currentUser, members, counts)
            }.collect { (currentUser, members, counts) ->
                setState {
                    copy(
                        currentUser = currentUser,
                        familyMembers = members,
                        memberExpenseCounts = counts,
                        editNameText = currentUser?.name ?: editNameText,
                        editRelationship = currentUser?.relationship ?: editRelationship
                    )
                }
            }
        }
    }

    override fun handleEvent(event: FamilyEvent) {
        when (event) {
            is FamilyEvent.OpenAddMemberDialog -> setState {
                copy(
                    isAddMemberDialogOpen = true,
                    newMemberName = "",
                    newMemberEmail = "",
                    errorMessage = null
                )
            }
            is FamilyEvent.CloseAddMemberDialog -> setState { copy(isAddMemberDialogOpen = false) }
            is FamilyEvent.NewMemberNameChanged -> setState { copy(newMemberName = event.name) }
            is FamilyEvent.NewMemberEmailChanged -> setState { copy(newMemberEmail = event.email) }
            is FamilyEvent.NewMemberRelationshipChanged -> setState { copy(newMemberRelationship = event.relationship) }
            is FamilyEvent.SubmitAddMember -> handleAddMember()

            is FamilyEvent.DeleteMember -> handleDeleteMember(event.memberId)

            is FamilyEvent.OpenEditProfileDialog -> setState {
                copy(
                    isEditProfileDialogOpen = true,
                    editNameText = currentUser?.name ?: "",
                    editRelationship = currentUser?.relationship ?: editRelationship
                )
            }
            is FamilyEvent.CloseEditProfileDialog -> setState { copy(isEditProfileDialogOpen = false) }
            is FamilyEvent.EditNameChanged -> setState { copy(editNameText = event.name) }
            is FamilyEvent.EditRelationshipChanged -> setState { copy(editRelationship = event.relationship) }
            is FamilyEvent.SubmitEditProfile -> handleEditProfile()

            is FamilyEvent.SignOut -> handleSignOut()
        }
    }

    private fun handleAddMember() {
        val state = currentState
        viewModelScope.launch {
            val result = addFamilyMemberUseCase(
                name = state.newMemberName,
                email = state.newMemberEmail,
                relationship = state.newMemberRelationship
            )
            if (result.isSuccess) {
                setState { copy(isAddMemberDialogOpen = false) }
                sendEffect(FamilyEffect.ShowToast("Added ${state.newMemberName} to family!"))
            } else {
                sendEffect(FamilyEffect.ShowToast(result.exceptionOrNull()?.message ?: "Failed to add member"))
            }
        }
    }

    private fun handleDeleteMember(memberId: String) {
        viewModelScope.launch {
            val result = removeFamilyMemberUseCase(memberId)
            if (result.isSuccess) {
                sendEffect(FamilyEffect.ShowToast("Family member removed"))
            } else {
                sendEffect(FamilyEffect.ShowToast(result.exceptionOrNull()?.message ?: "Failed to remove member"))
            }
        }
    }

    private fun handleEditProfile() {
        val state = currentState
        viewModelScope.launch {
            val result = updateProfileUseCase(
                name = state.editNameText,
                relationship = state.editRelationship
            )
            if (result.isSuccess) {
                setState { copy(isEditProfileDialogOpen = false) }
                sendEffect(FamilyEffect.ShowToast("Profile updated successfully"))
            } else {
                sendEffect(FamilyEffect.ShowToast("Failed to update profile"))
            }
        }
    }

    private fun handleSignOut() {
        viewModelScope.launch {
            logoutUseCase()
            sendEffect(FamilyEffect.NavigateToAuth)
        }
    }
}
