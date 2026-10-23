package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Category(
    val id: String,
    val displayName: String,
    val iconName: String,
    val defaultColorHex: String
) {
    FOOD_AND_DINING("food", "Food & Dining", "Restaurant", "#F59E0B"),
    TRAVEL_AND_TRANSPORT("travel", "Travel & Transport", "DirectionsCar", "#3B82F6"),
    SOFTWARE_AND_TECH("tech", "Software & Cloud", "Computer", "#8B5CF6"),
    OFFICE_AND_EQUIPMENT("office", "Office & Equipment", "BusinessCenter", "#10B981"),
    HEALTH_AND_WELLNESS("health", "Health & Wellness", "LocalHospital", "#EC4899"),
    ENTERTAINMENT("entertainment", "Entertainment", "Movie", "#6366F1"),
    UTILITIES_AND_BILLS("utilities", "Utilities & Bills", "ReceiptLong", "#14B8A6"),
    MISCELLANEOUS("misc", "Miscellaneous", "Category", "#64748B");

    companion object {
        fun fromId(id: String): Category {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MISCELLANEOUS
        }
    }
}
