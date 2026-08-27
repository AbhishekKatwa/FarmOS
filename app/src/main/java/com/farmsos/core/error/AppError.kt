package com.farmsos.core.error

sealed class AppError(val message: String) {
    data class NetworkError(val msg: String, val cause: Throwable? = null) : AppError(msg)
    data class DatabaseError(val msg: String, val cause: Throwable? = null) : AppError(msg)
    data class AuthenticationError(val msg: String) : AppError(msg)
    data class ValidationError(val fieldName: String, val msg: String) : AppError("$fieldName: $msg")
    data class ServerError(val code: Int, val msg: String) : AppError(msg)
    object UnknownError : AppError("An unknown error occurred")
}
