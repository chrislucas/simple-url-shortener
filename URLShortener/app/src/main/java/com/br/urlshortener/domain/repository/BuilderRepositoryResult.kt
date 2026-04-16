package com.br.urlshortener.domain.repository

sealed class BuilderRepositoryResult<out T> {
    data class Success<T>(val data: T) : BuilderRepositoryResult<T>()
    data class Error(val message: String, val code: Int? = null) : BuilderRepositoryResult<Nothing>()

    companion object {
        fun <T> onSuccess(data: T) = Success(data)
        fun onError(message: String, code: Int? = null) = Error(message, code)
    }
}
