package com.enterprise.expensetracker.domain.repository

import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<UserProfile?>
    suspend fun getCurrentUser(): UserProfile?
    
    suspend fun loginWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        relationship: FamilyRelationship
    ): Result<UserProfile>
    
    suspend fun loginWithGoogle(displayName: String?, email: String?): Result<UserProfile>
    suspend fun loginAsDemo(role: UserRole): Result<UserProfile>
    suspend fun logout(): Result<Unit>
    
    fun observeFamilyMembers(): Flow<List<UserProfile>>
    suspend fun addFamilyMember(
        name: String,
        email: String,
        relationship: FamilyRelationship
    ): Result<UserProfile>
    suspend fun removeFamilyMember(memberId: String): Result<Unit>
    suspend fun updateProfile(name: String, relationship: FamilyRelationship): Result<UserProfile>
}
