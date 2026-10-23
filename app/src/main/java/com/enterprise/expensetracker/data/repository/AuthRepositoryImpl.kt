package com.enterprise.expensetracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.database.MongoDbManager
import com.enterprise.expensetracker.core.database.entity.UserMongoEntity
import com.enterprise.expensetracker.core.database.entity.toDomain
import com.enterprise.expensetracker.core.database.entity.toMongoEntity
import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole
import com.enterprise.expensetracker.domain.repository.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Production Auth & Family Repository Implementation.
 * Live production testing with all profiles and memberships managed exclusively in MongoDB Atlas.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mongoDbManager: MongoDbManager,
    private val dispatchers: CoroutineDispatchers
) : AuthRepository {

    // Session pointer preference - holds only the active user session ID
    private val sessionPrefs: SharedPreferences =
        context.getSharedPreferences("household_auth_session", Context.MODE_PRIVATE)

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private val _sessionUserId: MutableStateFlow<String?>

    private val _currentUser = MutableStateFlow<UserProfile?>(null)

    init {
        // Clear any old mock demo sessions
        val savedSession = sessionPrefs.getString("session_user_id", null)
        val initialSession = if (savedSession in listOf("head_user_001", "member_user_002", "member_user_003")) {
            sessionPrefs.edit().remove("session_user_id").apply()
            null
        } else {
            savedSession
        }
        _sessionUserId = MutableStateFlow(initialSession)

        CoroutineScope(dispatchers.io).launch {
            // Purge leftover demo accounts from MongoDB store
            mongoDbManager.userDao.deleteUserById("head_user_001")
            mongoDbManager.userDao.deleteUserById("member_user_002")
            mongoDbManager.userDao.deleteUserById("member_user_003")

            // Purge any lingering demo session
            val currentSessionId = _sessionUserId.value
            if (!currentSessionId.isNullOrBlank()) {
                val user = mongoDbManager.userDao.getUserById(currentSessionId)
                if (user?.email == "google.user@household.in" || user?._id?.startsWith("head_user") == true) {
                    mongoDbManager.userDao.deleteUserById(currentSessionId)
                    setSession(null)
                    _currentUser.value = null
                }
            }

            // Continuously observe users from MongoDB and update current user based on active session
            combine(
                mongoDbManager.userDao.getAllUsersFlow(),
                _sessionUserId
            ) { users, activeId ->
                if (activeId.isNullOrBlank() || users.isEmpty()) {
                    null
                } else {
                    users.firstOrNull { it._id == activeId }?.toDomain()
                }
            }.collect { resolvedUser ->
                _currentUser.value = resolvedUser
            }
        }
    }

    private fun setSession(userId: String?) {
        _sessionUserId.value = userId
        sessionPrefs.edit().apply {
            if (userId != null) {
                putString("session_user_id", userId)
            } else {
                remove("session_user_id")
            }
            apply()
        }
    }

    override fun observeCurrentUser(): Flow<UserProfile?> = _currentUser.asStateFlow()

    override suspend fun getCurrentUser(): UserProfile? = withContext(dispatchers.io) {
        _currentUser.value ?: run {
            val session = _sessionUserId.value
            if (!session.isNullOrBlank()) {
                mongoDbManager.userDao.getUserById(session)?.toDomain()
            } else {
                null
            }
        }
    }

    override fun observeFamilyMembers(): Flow<List<UserProfile>> {
        return mongoDbManager.userDao.getAllUsersFlow().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(dispatchers.io)
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        return withContext(dispatchers.io) {
            try {
                val cleanEmail = email.trim()
                if (cleanEmail.isBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("Email cannot be empty"))
                }
                val passwordHash = password.hashCode().toString()

                // Check if user exists in MongoDB
                var userEntity = mongoDbManager.userDao.getUserByEmail(cleanEmail)

                if (userEntity != null) {
                    if (!userEntity.passwordHash.isNullOrBlank() && userEntity.passwordHash != passwordHash) {
                        return@withContext Result.failure(IllegalArgumentException("Incorrect password for $cleanEmail"))
                    }
                } else {
                    // Auto-create account directly in MongoDB Atlas
                    val newEntity = UserMongoEntity(
                        _id = UUID.randomUUID().toString(),
                        name = cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords(),
                        email = cleanEmail,
                        passwordHash = passwordHash,
                        role = UserRole.HEAD_OF_HOUSEHOLD.name,
                        relationship = FamilyRelationship.HEAD.name,
                        familyId = "family_${UUID.randomUUID().toString().take(8)}",
                        authProvider = "PASSWORD",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    mongoDbManager.userDao.upsertUser(newEntity)
                    userEntity = newEntity
                }

                val profile = userEntity.toDomain()
                setSession(profile.id)
                _currentUser.value = profile
                Result.success(profile)
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

                // Check MongoDB if email already registered
                val existing = mongoDbManager.userDao.getUserByEmail(cleanEmail)
                if (existing != null) {
                    return@withContext Result.failure(IllegalArgumentException("Email is already registered in MongoDB"))
                }

                val familyId = if (role == UserRole.HEAD_OF_HOUSEHOLD) {
                    "family_${UUID.randomUUID().toString().take(8)}"
                } else {
                    _currentUser.value?.familyId ?: "family_primary"
                }

                val entity = UserMongoEntity(
                    _id = UUID.randomUUID().toString(),
                    name = cleanName,
                    email = cleanEmail,
                    passwordHash = password.hashCode().toString(),
                    role = role.name,
                    relationship = relationship.name,
                    familyId = familyId,
                    authProvider = "PASSWORD",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                // Save directly to MongoDB Atlas
                mongoDbManager.userDao.upsertUser(entity)

                val profile = entity.toDomain()
                setSession(profile.id)
                _currentUser.value = profile
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(Exception(e.localizedMessage ?: "Failed to create account"))
            }
        }
    }

    override suspend fun loginWithGoogle(displayName: String?, email: String?): Result<UserProfile> {
        return withContext(dispatchers.io) {
            try {
                val cleanEmail = email?.trim()
                if (cleanEmail.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("Google account email is required"))
                }
                val resolvedName = displayName?.trim().takeIf { !it.isNullOrBlank() }
                    ?: cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords()

                val existing = mongoDbManager.userDao.getUserByEmail(cleanEmail)
                val entity = existing ?: run {
                    val newEntity = UserMongoEntity(
                        _id = UUID.randomUUID().toString(),
                        name = resolvedName,
                        email = cleanEmail,
                        role = UserRole.HEAD_OF_HOUSEHOLD.name,
                        relationship = FamilyRelationship.HEAD.name,
                        familyId = _currentUser.value?.familyId ?: "family_google_${UUID.randomUUID().toString().take(6)}",
                        authProvider = "GOOGLE",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    mongoDbManager.userDao.upsertUser(newEntity)
                    newEntity
                }

                val profile = entity.toDomain()
                setSession(profile.id)
                _currentUser.value = profile
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(Exception("Google Sign-In failed: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun loginAsDemo(role: UserRole): Result<UserProfile> {
        return Result.failure(UnsupportedOperationException("Demo accounts disabled for live production testing"))
    }

    override suspend fun logout(): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                firebaseAuth?.signOut()
                setSession(null)
                _currentUser.value = null
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
            val existing = mongoDbManager.userDao.getUserByEmail(cleanEmail)
            if (existing != null) {
                return@withContext Result.failure(IllegalArgumentException("A family member with this email already exists in MongoDB"))
            }

            val newEntity = UserMongoEntity(
                _id = UUID.randomUUID().toString(),
                name = name.trim(),
                email = cleanEmail,
                role = UserRole.FAMILY_MEMBER.name,
                relationship = relationship.name,
                familyId = current.familyId,
                authProvider = "PASSWORD",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Persist exclusively in MongoDB Atlas
            mongoDbManager.userDao.upsertUser(newEntity)

            Result.success(newEntity.toDomain())
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

            // Remove directly from MongoDB Atlas
            mongoDbManager.userDao.deleteUserById(memberId)
            Result.success(Unit)
        }
    }

    override suspend fun updateProfile(
        name: String,
        relationship: FamilyRelationship
    ): Result<UserProfile> {
        return withContext(dispatchers.io) {
            val current = _currentUser.value ?: return@withContext Result.failure(IllegalStateException("No active session"))
            val existing = mongoDbManager.userDao.getUserById(current.id)
            val updatedEntity = (existing ?: current.toMongoEntity()).copy(
                name = name.trim().ifEmpty { current.name },
                relationship = relationship.name,
                updatedAt = System.currentTimeMillis()
            )

            // Update exclusively in MongoDB Atlas
            mongoDbManager.userDao.upsertUser(updatedEntity)
            val updated = updatedEntity.toDomain()
            _currentUser.value = updated
            Result.success(updated)
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
