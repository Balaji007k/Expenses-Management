package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Currency(val code: String, val symbol: String, val displayName: String) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    INR("INR", "₹", "Indian Rupee"),
    JPY("JPY", "¥", "Japanese Yen"),
    CAD("CAD", "C$", "Canadian Dollar"),
    AUD("AUD", "A$", "Australian Dollar");

    companion object {
        fun fromCode(code: String): Currency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }
}
