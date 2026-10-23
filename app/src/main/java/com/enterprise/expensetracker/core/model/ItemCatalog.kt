package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing globally saved catalog items (e.g. Tomato, Milk, Eggs).
 * Shared across all family members so common items, images, and categories are auto-populated.
 */
@Serializable
data class ItemCatalog(
    val id: String,
    val name: String,
    val categoryId: String = "groceries_and_food",
    val imageUrl: String? = null,
    val defaultUnit: String = "kg",
    val lastPrice: Double = 0.0,
    val lastQuantity: Double = 1.0,
    val updatedAt: Long = System.currentTimeMillis()
)
