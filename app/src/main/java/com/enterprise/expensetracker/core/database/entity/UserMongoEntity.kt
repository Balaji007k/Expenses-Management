package com.enterprise.expensetracker.core.database.entity

import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserMongoEntity(
    var _id: String = "",
    var name: String = "",
    var email: String = "",
    var passwordHash: String = "",
    var role: String = "HEAD_OF_HOUSEHOLD",
    var relationship: String = "HEAD",
    var familyId: String = "family_primary",
    var authProvider: String = "PASSWORD",
    var avatarUrl: String? = null,
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
)

fun UserProfile.toMongoEntity(passwordHash: String = ""): UserMongoEntity {
    return UserMongoEntity(
        _id = id,
        name = name,
        email = email,
        passwordHash = passwordHash,
        role = role.name,
        relationship = relationship.name,
        familyId = familyId,
        authProvider = authProvider,
        avatarUrl = avatarUrl,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}

fun UserMongoEntity.toDomain(): UserProfile {
    return UserProfile(
        id = _id,
        name = name,
        email = email,
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.HEAD_OF_HOUSEHOLD),
        relationship = FamilyRelationship.fromString(relationship),
        familyId = familyId,
        avatarUrl = avatarUrl,
        authProvider = authProvider,
        createdAt = createdAt
    )
}
