package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Currency(val code: String, val symbol: String, val displayName: String) {
    INR("INR", "₹", "Indian Rupee");

    companion object {
        fun fromCode(code: String): Currency {
            return INR
        }
    }
}
