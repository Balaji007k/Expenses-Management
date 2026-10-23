package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val role: UserRole = UserRole.HEAD_OF_HOUSEHOLD,
    val relationship: FamilyRelationship = FamilyRelationship.HEAD,
    val familyId: String = "family_primary",
    val avatarUrl: String? = null,
    val authProvider: String = "PASSWORD", // "PASSWORD" or "GOOGLE"
    val createdAt: Long = System.currentTimeMillis()
) {
    val isHeadOfHousehold: Boolean
        get() = role == UserRole.HEAD_OF_HOUSEHOLD
}
