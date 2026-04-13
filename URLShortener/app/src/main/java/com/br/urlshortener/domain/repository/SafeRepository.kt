package com.br.urlshortener.domain.repository

import com.google.gson.Gson
import retrofit2.Response

object SafeRepository {

    suspend fun <ResponseBody, Domain> remoteCall(
        call: suspend () -> Response<ResponseBody>,
        onSuccess: (ResponseBody) -> Domain
    ): RepositoryResult<Domain> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    RepositoryResult.onSuccess(onSuccess(responseBody))
                } ?: run {
                    RepositoryResult.onError("null_body", response.code())
                }
            } else {
                val message = Gson().fromJson(
                    response.errorBody()?.string() ?: "empty_message",
                    String::class.java
                )
                RepositoryResult.onError(message, response.code())
            }
        } catch (e: Exception) {
            RepositoryResult.onError(e.message ?: "error on safeCallRemote. There is not message", 500)
        }
    }
}
