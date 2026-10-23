package com.enterprise.expensetracker.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    SYNC_FAILED
}
