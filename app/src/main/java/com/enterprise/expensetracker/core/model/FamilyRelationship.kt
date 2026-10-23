package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class FamilyRelationship(val displayName: String) {
    HEAD("Head of Household"),
    SPOUSE("Spouse"),
    CHILD("Child"),
    PARENT("Parent"),
    SIBLING("Sibling"),
    OTHER("Other Relative");

    companion object {
        fun fromString(value: String): FamilyRelationship {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
