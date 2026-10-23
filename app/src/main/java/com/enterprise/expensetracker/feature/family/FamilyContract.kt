package com.enterprise.expensetracker.feature.family

import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile

data class FamilyState(
    val isLoading: Boolean = false,
    val currentUser: UserProfile? = null,
    val familyMembers: List<UserProfile> = emptyList(),
    val isAddMemberDialogOpen: Boolean = false,
    val isEditProfileDialogOpen: Boolean = false,
    val editNameText: String = "",
    val editRelationship: FamilyRelationship = FamilyRelationship.OTHER,
    val newMemberName: String = "",
    val newMemberEmail: String = "",
    val newMemberRelationship: FamilyRelationship = FamilyRelationship.SPOUSE,
    val memberExpenseCounts: Map<String, Int> = emptyMap(),
    val errorMessage: String? = null
)

sealed interface FamilyEvent {
    data object OpenAddMemberDialog : FamilyEvent
    data object CloseAddMemberDialog : FamilyEvent
    data class NewMemberNameChanged(val name: String) : FamilyEvent
    data class NewMemberEmailChanged(val email: String) : FamilyEvent
    data class NewMemberRelationshipChanged(val relationship: FamilyRelationship) : FamilyEvent
    data object SubmitAddMember : FamilyEvent

    data class DeleteMember(val memberId: String) : FamilyEvent

    data object OpenEditProfileDialog : FamilyEvent
    data object CloseEditProfileDialog : FamilyEvent
    data class EditNameChanged(val name: String) : FamilyEvent
    data class EditRelationshipChanged(val relationship: FamilyRelationship) : FamilyEvent
    data object SubmitEditProfile : FamilyEvent

    data object SignOut : FamilyEvent
}

sealed interface FamilyEffect {
    data class ShowToast(val message: String) : FamilyEffect
    data object NavigateToAuth : FamilyEffect
}
