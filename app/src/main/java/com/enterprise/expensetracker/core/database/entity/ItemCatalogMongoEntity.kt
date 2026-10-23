package com.enterprise.expensetracker.core.database.entity

import com.enterprise.expensetracker.core.model.ItemCatalog
import kotlinx.serialization.Serializable

@Serializable
data class ItemCatalogMongoEntity(
    var _id: String = "",
    var name: String = "",
    var categoryId: String = "groceries_and_food",
    var imageUrl: String? = null,
    var defaultUnit: String = "kg",
    var lastPrice: Double = 0.0,
    var lastQuantity: Double = 1.0,
    var updatedAt: Long = 0L
)

fun ItemCatalog.toMongoEntity(): ItemCatalogMongoEntity {
    return ItemCatalogMongoEntity(
        _id = id,
        name = name,
        categoryId = categoryId,
        imageUrl = imageUrl,
        defaultUnit = defaultUnit,
        lastPrice = lastPrice,
        lastQuantity = lastQuantity,
        updatedAt = updatedAt
    )
}

fun ItemCatalogMongoEntity.toDomain(): ItemCatalog {
    return ItemCatalog(
        id = _id,
        name = name,
        categoryId = categoryId,
        imageUrl = imageUrl,
        defaultUnit = defaultUnit.ifBlank { "kg" },
        lastPrice = lastPrice,
        lastQuantity = lastQuantity,
        updatedAt = updatedAt
    )
}
