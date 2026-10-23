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
 * All user profiles, roles, and family memberships are stored and managed exclusively in MongoDB Atlas.
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

    private val _sessionUserId = MutableStateFlow<String?>(
        sessionPrefs.getString("session_user_id", "head_user_001")
    )

    private val _currentUser = MutableStateFlow<UserProfile?>(null)

    init {
        CoroutineScope(dispatchers.io).launch {
            // Seed default household users in MongoDB if store is completely empty
            ensureDefaultHouseholdInMongo()

            // Continuously observe users from MongoDB and update current user based on active session
            combine(
                mongoDbManager.userDao.getAllUsersFlow(),
                _sessionUserId
            ) { users, activeId ->
                if (users.isEmpty()) {
                    null
                } else {
                    val active = if (activeId != null) {
                        users.firstOrNull { it._id == activeId }
                    } else {
                        users.firstOrNull { it.role == UserRole.HEAD_OF_HOUSEHOLD.name } ?: users.firstOrNull()
                    }
                    active?.toDomain()
                }
            }.collect { resolvedUser ->
                _currentUser.value = resolvedUser
            }
        }
    }

    private suspend fun ensureDefaultHouseholdInMongo() {
        val existingHead = mongoDbManager.userDao.getUserById("head_user_001")
        if (existingHead == null) {
            val familyId = "family_sharma_primary"
            val defaultUsers = listOf(
                UserMongoEntity(
                    _id = "head_user_001",
                    name = "Ramesh Sharma",
                    email = "ramesh.sharma@home.in",
                    passwordHash = "password".hashCode().toString(),
                    role = UserRole.HEAD_OF_HOUSEHOLD.name,
                    relationship = FamilyRelationship.HEAD.name,
                    familyId = familyId,
                    authProvider = "PASSWORD",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                UserMongoEntity(
                    _id = "member_user_002",
                    name = "Priya Sharma",
                    email = "priya.sharma@home.in",
                    passwordHash = "password".hashCode().toString(),
                    role = UserRole.FAMILY_MEMBER.name,
                    relationship = FamilyRelationship.SPOUSE.name,
                    familyId = familyId,
                    authProvider = "PASSWORD",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                UserMongoEntity(
                    _id = "member_user_003",
                    name = "Aarav Sharma",
                    email = "aarav.sharma@home.in",
                    passwordHash = "password".hashCode().toString(),
                    role = UserRole.FAMILY_MEMBER.name,
                    relationship = FamilyRelationship.CHILD.name,
                    familyId = familyId,
                    authProvider = "PASSWORD",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            defaultUsers.forEach { mongoDbManager.userDao.upsertUser(it) }
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
            if (session != null) {
                mongoDbManager.userDao.getUserById(session)?.toDomain()
            } else {
                mongoDbManager.userDao.getUserById("head_user_001")?.toDomain()
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
                // Check if user exists in MongoDB
                var userEntity = mongoDbManager.userDao.getUserByEmail(cleanEmail)

                // Try Firebase Auth if configured
                firebaseAuth?.let { auth ->
                    try {
                        auth.signInWithEmailAndPassword(cleanEmail, password).await()
                    } catch (fbEx: Exception) {
                        // Allow login if user already exists in MongoDB
                        if (userEntity == null) {
                            throw fbEx
                        }
                    }
                }

                // If not found in MongoDB, create new account in MongoDB
                if (userEntity == null) {
                    val newEntity = UserMongoEntity(
                        _id = UUID.randomUUID().toString(),
                        name = cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords(),
                        email = cleanEmail,
                        passwordHash = password.hashCode().toString(),
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

                // Firebase Auth user registration
                firebaseAuth?.let { auth ->
                    try {
                        val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                        authResult.user?.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(cleanName)
                                .build()
                        )?.await()
                    } catch (_: Exception) {
                        // In development or test environments, proceed to MongoDB persistence
                    }
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

                // Save directly to MongoDB
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
                val resolvedEmail = email?.trim() ?: "google.user@household.in"
                val resolvedName = displayName?.trim() ?: resolvedEmail.substringBefore("@").replace(".", " ").capitalizeWords()

                val existing = mongoDbManager.userDao.getUserByEmail(resolvedEmail)
                val entity = existing ?: run {
                    val newEntity = UserMongoEntity(
                        _id = UUID.randomUUID().toString(),
                        name = resolvedName,
                        email = resolvedEmail,
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
        return withContext(dispatchers.io) {
            ensureDefaultHouseholdInMongo()
            val allUsers = mongoDbManager.userDao.getAllUsersFlow()
            // Select user by role from MongoDB
            val target = if (role == UserRole.HEAD_OF_HOUSEHOLD) {
                mongoDbManager.userDao.getUserById("head_user_001")
            } else {
                mongoDbManager.userDao.getUserById("member_user_002")
            }

            if (target != null) {
                val profile = target.toDomain()
                setSession(profile.id)
                _currentUser.value = profile
                Result.success(profile)
            } else {
                Result.failure(Exception("Demo user not found in MongoDB"))
            }
        }
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
