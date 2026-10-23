package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(val displayName: String, val description: String) {
    HEAD_OF_HOUSEHOLD("Head of Household", "Full access to manage all family expenses, members, and household budgets"),
    FAMILY_MEMBER("Family Member", "Can record and manage their own expenses and personal profile")
}
