package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Category(
    val id: String,
    val displayName: String,
    val iconName: String,
    val defaultColorHex: String
) {
    GROCERIES_AND_FOOD("groceries", "Groceries & Food", "ShoppingBasket", "#10B981"),
    RENT_AND_HOUSING("housing", "Rent & Housing", "Home", "#3B82F6"),
    UTILITIES_AND_BILLS("utilities", "Electricity & Bills", "Bolt", "#F59E0B"),
    INTERNET_AND_MOBILE("internet", "Internet & Mobile", "Wifi", "#06B6D4"),
    KIDS_AND_EDUCATION("education", "Kids & Education", "School", "#8B5CF6"),
    HEALTH_AND_MEDICINE("health", "Health & Medicines", "LocalHospital", "#EC4899"),
    HOME_MAINTENANCE("repairs", "Repairs & Maintenance", "Build", "#F97316"),
    SHOPPING_AND_CLOTHING("shopping", "Shopping & Clothes", "ShoppingBag", "#6366F1"),
    TRAVEL_AND_FUEL("travel", "Fuel & Commute", "DirectionsCar", "#475569"),
    DINING_AND_OUTING("dining", "Dining Out & Treats", "Restaurant", "#E11D48"),
    MISCELLANEOUS("misc", "Other Home Expenses", "Category", "#94A3B8");

    companion object {
        fun fromId(id: String): Category {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
                ?: when (id.lowercase()) {
                    "food" -> GROCERIES_AND_FOOD
                    "office", "tech" -> HOME_MAINTENANCE
                    "entertainment" -> DINING_AND_OUTING
                    else -> MISCELLANEOUS
                }
        }
    }
}
