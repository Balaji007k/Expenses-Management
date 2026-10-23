package com.enterprise.expensetracker.domain.usecase

import com.enterprise.expensetracker.core.model.ItemCatalog
import com.enterprise.expensetracker.domain.repository.ItemCatalogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCatalogItemsUseCase @Inject constructor(
    private val itemCatalogRepository: ItemCatalogRepository
) {
    operator fun invoke(): Flow<List<ItemCatalog>> {
        return itemCatalogRepository.observeAllCatalogItems()
    }

    suspend fun search(query: String): List<ItemCatalog> {
        return itemCatalogRepository.searchCatalog(query)
    }
}

class SaveCatalogItemUseCase @Inject constructor(
    private val itemCatalogRepository: ItemCatalogRepository
) {
    suspend operator fun invoke(item: ItemCatalog): Result<Unit> {
        return itemCatalogRepository.saveCatalogItem(item)
    }
}
