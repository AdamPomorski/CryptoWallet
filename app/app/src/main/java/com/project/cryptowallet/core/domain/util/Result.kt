package com.project.cryptowallet.core.domain.util

typealias DomainError = Error

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: DomainError>(val error: E): Result<Nothing, E>
    data class Loading(val isLoading: Boolean = true): Result<Nothing, Nothing>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
        is Result.Loading -> Result.Loading(isLoading)
    }
}
inline fun <T, E: Error, R> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> {
    return when (this) {
        is Result.Success -> transform(data)  // Apply transformation if success
        is Result.Error -> this // Propagate error if failed
        is Result.Loading -> Result.Loading(isLoading)
    }
}


fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
        is Result.Loading -> Result.Loading(isLoading)
    }
}
inline fun <T, E: Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
        is Result.Loading -> Result.Loading(isLoading)
    }
}

typealias EmptyResult<E> = Result<Unit, E>