package com.enterprise.expensetracker.core.database.dao

import com.enterprise.expensetracker.core.database.entity.UserMongoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Users & Family Members in MongoDB Atlas.
 */
interface UserMongoDao {
    fun getAllUsersFlow(): Flow<List<UserMongoEntity>>
    fun getFamilyMembersFlow(familyId: String): Flow<List<UserMongoEntity>>
    suspend fun getUserById(id: String): UserMongoEntity?
    suspend fun getUserByEmail(email: String): UserMongoEntity?
    suspend fun upsertUser(entity: UserMongoEntity)
    suspend fun upsertUsers(entities: List<UserMongoEntity>)
    suspend fun deleteUserById(id: String)
    suspend fun clearAll()
}
