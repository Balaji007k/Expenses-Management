package com.enterprise.expensetracker.domain.repository

import com.enterprise.expensetracker.core.model.ItemCatalog
import kotlinx.coroutines.flow.Flow

interface ItemCatalogRepository {
    fun observeAllCatalogItems(): Flow<List<ItemCatalog>>
    suspend fun searchCatalog(query: String): List<ItemCatalog>
    suspend fun saveCatalogItem(item: ItemCatalog): Result<Unit>
}
