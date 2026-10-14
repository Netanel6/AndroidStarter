package com.example.androidstarter.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

enum class AppError { Network, Unauthorized, Rejected, NotFound, Server, InvalidData }
