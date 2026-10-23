package com.enterprise.expensetracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole
import com.enterprise.expensetracker.domain.repository.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers
) : AuthRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("household_auth_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    private val _familyMembers = MutableStateFlow<List<UserProfile>>(emptyList())

    init {
        loadPersistedState()
    }

    private fun loadPersistedState() {
        val savedUserJson = prefs.getString("current_user_profile", null)
        val savedMembersJson = prefs.getString("family_members_list", null)

        val initialMembers = if (!savedMembersJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<UserProfile>>(savedMembersJson)
            } catch (e: Exception) {
                createDefaultFamilyMembers()
            }
        } else {
            createDefaultFamilyMembers()
        }
        _familyMembers.value = initialMembers

        val activeUser = if (!savedUserJson.isNullOrBlank()) {
            try {
                json.decodeFromString<UserProfile>(savedUserJson)
            } catch (e: Exception) {
                initialMembers.firstOrNull { it.role == UserRole.HEAD_OF_HOUSEHOLD } ?: initialMembers.first()
            }
        } else {
            // Default active user is the Head of Household
            initialMembers.firstOrNull { it.role == UserRole.HEAD_OF_HOUSEHOLD } ?: initialMembers.first()
        }

        _currentUser.value = activeUser
        persistState()
    }

    private fun createDefaultFamilyMembers(): List<UserProfile> {
        val headId = "head_user_001"
        val familyId = "family_sharma_primary"
        return listOf(
            UserProfile(
                id = headId,
                name = "Ramesh Sharma",
                email = "ramesh.sharma@home.in",
                role = UserRole.HEAD_OF_HOUSEHOLD,
                relationship = FamilyRelationship.HEAD,
                familyId = familyId,
                authProvider = "PASSWORD"
            ),
            UserProfile(
                id = "member_user_002",
                name = "Priya Sharma",
                email = "priya.sharma@home.in",
                role = UserRole.FAMILY_MEMBER,
                relationship = FamilyRelationship.SPOUSE,
                familyId = familyId,
                authProvider = "PASSWORD"
            ),
            UserProfile(
                id = "member_user_003",
                name = "Aarav Sharma",
                email = "aarav.sharma@home.in",
                role = UserRole.FAMILY_MEMBER,
                relationship = FamilyRelationship.CHILD,
                familyId = familyId,
                authProvider = "PASSWORD"
            )
        )
    }

    private fun persistState() {
        prefs.edit().apply {
            _currentUser.value?.let {
                putString("current_user_profile", json.encodeToString(it))
            } ?: remove("current_user_profile")
            putString("family_members_list", json.encodeToString(_familyMembers.value))
            apply()
        }
    }

    override fun observeCurrentUser(): Flow<UserProfile?> = _currentUser.asStateFlow()

    override suspend fun getCurrentUser(): UserProfile? = _currentUser.value

    override fun observeFamilyMembers(): Flow<List<UserProfile>> = _familyMembers.asStateFlow()

    override suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        return withContext(dispatchers.io) {
            try {
                val cleanEmail = email.trim()
                // Check if existing in family roster first
                val existing = _familyMembers.value.firstOrNull { it.email.equals(cleanEmail, ignoreCase = true) }
                
                // Attempt Firebase Auth sign-in if available
                firebaseAuth?.let { auth ->
                    try {
                        auth.signInWithEmailAndPassword(cleanEmail, password).await()
                    } catch (fbEx: Exception) {
                        // If Firebase fails (e.g. offline or unconfigured test email), allow login if existing in local family
                        if (existing == null) {
                            throw fbEx
                        }
                    }
                }

                val loggedIn = existing ?: UserProfile(
                    id = UUID.randomUUID().toString(),
                    name = cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords(),
                    email = cleanEmail,
                    role = UserRole.HEAD_OF_HOUSEHOLD,
                    relationship = FamilyRelationship.HEAD,
                    familyId = _currentUser.value?.familyId ?: "family_primary",
                    authProvider = "PASSWORD"
                )

                if (existing == null) {
                    val updated = _familyMembers.value + loggedIn
                    _familyMembers.value = updated
                }

                _currentUser.value = loggedIn
                persistState()
                Result.success(loggedIn)
            } catch (e: Exception) {
                Result.failure(Exception(e.localizedMessage ?: "Invalid email or password"))
            }
        }
    }

    override suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        relationship: FamilyRelationship
    ): Result<UserProfile> {
        return withContext(dispatchers.io) {
            try {
                val cleanEmail = email.trim()
                val cleanName = name.trim()
                
                // Try Firebase create user
                firebaseAuth?.let { auth ->
                    try {
                        val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                        authResult.user?.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(cleanName)
                                .build()
                        )?.await()
                    } catch (fbEx: Exception) {
                        // Allow local registration if Firebase is in development/offline mode
                    }
                }

                val familyId = if (role == UserRole.HEAD_OF_HOUSEHOLD) {
                    "family_${UUID.randomUUID().toString().take(8)}"
                } else {
                    _currentUser.value?.familyId ?: "family_primary"
                }

                val newProfile = UserProfile(
                    id = UUID.randomUUID().toString(),
                    name = cleanName,
                    email = cleanEmail,
                    role = role,
                    relationship = relationship,
                    familyId = familyId,
                    authProvider = "PASSWORD"
                )

                val updatedMembers = _familyMembers.value.filter { !it.email.equals(cleanEmail, ignoreCase = true) } + newProfile
                _familyMembers.value = updatedMembers
                _currentUser.value = newProfile
                persistState()

                Result.success(newProfile)
            } catch (e: Exception) {
                Result.failure(Exception(e.localizedMessage ?: "Failed to create account"))
            }
        }
    }

    override suspend fun loginWithGoogle(displayName: String?, email: String?): Result<UserProfile> {
        return withContext(dispatchers.io) {
            try {
                val resolvedEmail = email?.trim() ?: "google.user@household.in"
                val resolvedName = displayName?.trim() ?: resolvedEmail.substringBefore("@").replace(".", " ").capitalizeWords()

                val existing = _familyMembers.value.firstOrNull { it.email.equals(resolvedEmail, ignoreCase = true) }
                val user = existing ?: UserProfile(
                    id = UUID.randomUUID().toString(),
                    name = resolvedName,
                    email = resolvedEmail,
                    role = UserRole.HEAD_OF_HOUSEHOLD,
                    relationship = FamilyRelationship.HEAD,
                    familyId = _currentUser.value?.familyId ?: "family_google_${UUID.randomUUID().toString().take(6)}",
                    authProvider = "GOOGLE"
                )

                if (existing == null) {
                    _familyMembers.value = _familyMembers.value + user
                }

                _currentUser.value = user
                persistState()
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(Exception("Google Sign-In failed: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun loginAsDemo(role: UserRole): Result<UserProfile> {
        return withContext(dispatchers.io) {
            val user = if (role == UserRole.HEAD_OF_HOUSEHOLD) {
                _familyMembers.value.firstOrNull { it.role == UserRole.HEAD_OF_HOUSEHOLD }
                    ?: createDefaultFamilyMembers().first()
            } else {
                _familyMembers.value.firstOrNull { it.role == UserRole.FAMILY_MEMBER }
                    ?: createDefaultFamilyMembers()[1]
            }

            _currentUser.value = user
            persistState()
            Result.success(user)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                firebaseAuth?.signOut()
                _currentUser.value = null
                prefs.edit().remove("current_user_profile").apply()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun addFamilyMember(
        name: String,
        email: String,
        relationship: FamilyRelationship
    ): Result<UserProfile> {
        return withContext(dispatchers.io) {
            val current = _currentUser.value
            if (current == null || !current.isHeadOfHousehold) {
                return@withContext Result.failure(IllegalAccessException("Only Head of Household can add family members"))
            }

            val cleanEmail = email.trim()
            if (_familyMembers.value.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
                return@withContext Result.failure(IllegalArgumentException("A family member with this email already exists"))
            }

            val newMember = UserProfile(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                email = cleanEmail,
                role = UserRole.FAMILY_MEMBER,
                relationship = relationship,
                familyId = current.familyId,
                authProvider = "PASSWORD"
            )

            _familyMembers.value = _familyMembers.value + newMember
            persistState()
            Result.success(newMember)
        }
    }

    override suspend fun removeFamilyMember(memberId: String): Result<Unit> {
        return withContext(dispatchers.io) {
            val current = _currentUser.value
            if (current == null || !current.isHeadOfHousehold) {
                return@withContext Result.failure(IllegalAccessException("Only Head of Household can remove family members"))
            }

            if (current.id == memberId) {
                return@withContext Result.failure(IllegalArgumentException("Cannot remove the Head of Household"))
            }

            _familyMembers.value = _familyMembers.value.filter { it.id != memberId }
            persistState()
            Result.success(Unit)
        }
    }

    override suspend fun updateProfile(
        name: String,
        relationship: FamilyRelationship
    ): Result<UserProfile> {
        return withContext(dispatchers.io) {
            val current = _currentUser.value ?: return@withContext Result.failure(IllegalStateException("No active session"))
            val updated = current.copy(
                name = name.trim().ifEmpty { current.name },
                relationship = relationship
            )

            _currentUser.value = updated
            _familyMembers.value = _familyMembers.value.map { if (it.id == updated.id) updated else it }
            persistState()
            Result.success(updated)
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
