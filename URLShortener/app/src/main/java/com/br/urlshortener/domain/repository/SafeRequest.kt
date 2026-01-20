package com.br.urlshortener.domain.repository

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import retrofit2.Response

/**
 * TODO criar um object similar para outras operacoes que nao estejam vinculadas
 * com retrofit
 */

object SafeRequest {
    suspend inline operator fun <T, reified E> invoke(
        operation: suspend () -> Response<T>,
        onError: (Throwable) -> E
    ): OperationResult<T, E> = try {
        val response = operation()
        if (response.isSuccessful) {
            response.body()?.let {
                OperationResult.success(it)
            } ?: OperationResult.failure(
                onError(Exception("empty body response"))
            )
        } else {
            val message: E = Gson()
                .fromJson(
                    response.errorBody()?.charStream(),
                    E::class.java
                )
            OperationResult.failure(message = message)
        }
    } catch (e: Exception) {
        OperationResult.failure(message = onError(e))
    }
}

data class OperationResult<out T, out E>(
    val status: OperationStatus,
    val data: T? = null,
    val message: E? = null
) {
    companion object {
        fun <T> success(data: T): OperationResult<T, Nothing> =
            OperationResult(OperationStatus.Successful, data)

        fun <T, E> failure(message: E, data: T? = null) =
            OperationResult(OperationStatus.Failure, data, message)

        fun <T> idle(data: T? = null) = OperationResult(OperationStatus.Idle, data, null)
    }
}

sealed class OperationStatus {
    data object Successful : OperationStatus()
    data object Idle : OperationStatus()
    data object Failure : OperationStatus()
}

private fun test() {
    runBlocking {
        SafeRequest<String, Throwable>({
            Response.success("")
        }, { it })
    }
}
