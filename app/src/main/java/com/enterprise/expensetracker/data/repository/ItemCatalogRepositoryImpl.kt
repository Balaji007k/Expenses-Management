package com.enterprise.expensetracker.data.repository

import com.enterprise.expensetracker.core.common.dispatchers.CoroutineDispatchers
import com.enterprise.expensetracker.core.database.MongoDbManager
import com.enterprise.expensetracker.core.database.entity.toDomain
import com.enterprise.expensetracker.core.database.entity.toMongoEntity
import com.enterprise.expensetracker.core.model.ItemCatalog
import com.enterprise.expensetracker.domain.repository.ItemCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemCatalogRepositoryImpl @Inject constructor(
    private val mongoDbManager: MongoDbManager,
    private val dispatchers: CoroutineDispatchers
) : ItemCatalogRepository {

    override fun observeAllCatalogItems(): Flow<List<ItemCatalog>> {
        return mongoDbManager.itemCatalogDao.getAllCatalogItemsFlow()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun searchCatalog(query: String): List<ItemCatalog> {
        return withContext(dispatchers.io) {
            mongoDbManager.itemCatalogDao.searchCatalog(query).map { it.toDomain() }
        }
    }

    override suspend fun saveCatalogItem(item: ItemCatalog): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                mongoDbManager.itemCatalogDao.upsertCatalogItem(item.toMongoEntity())
                Result.success(Unit)
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
    }
}
