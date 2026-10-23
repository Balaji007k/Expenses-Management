package com.enterprise.expensetracker.core.common.result

/**
 * Enterprise standard wrapper for UI and Domain layer data exchange.
 * Represents state of any asynchronous operation (Loading, Success, Error).
 */
sealed interface Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> Resource.Error(message, throwable)
        is Resource.Loading -> Resource.Loading
    }
}
