package com.enterprise.expensetracker.core.database.dao

import com.enterprise.expensetracker.core.database.entity.ItemCatalogMongoEntity
import kotlinx.coroutines.flow.Flow

interface ItemCatalogMongoDao {
    fun getAllCatalogItemsFlow(): Flow<List<ItemCatalogMongoEntity>>
    suspend fun searchCatalog(query: String): List<ItemCatalogMongoEntity>
    suspend fun getCatalogItemById(id: String): ItemCatalogMongoEntity?
    suspend fun upsertCatalogItem(entity: ItemCatalogMongoEntity)
    suspend fun deleteCatalogItem(id: String)
}
