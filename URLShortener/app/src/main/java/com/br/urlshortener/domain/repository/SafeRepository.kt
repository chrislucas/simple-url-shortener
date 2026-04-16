package com.br.urlshortener.domain.repository

import com.br.urlshortener.data.remote.model.ErrorResponseDTO
import com.google.gson.Gson
import retrofit2.Response

object SafeRepository {

    private const val NULL_BODY_MESSAGE = "null_body"
    private const val EMPTY_ERROR_MESSAGE = "empty_message"
    private const val GENERIC_ERROR_MESSAGE = "error on safeCallRemote. There is not message on exception class"

    suspend fun <ResponseBody, Domain> remoteCall(
        call: suspend () -> Response<ResponseBody>,
        onSuccess: (ResponseBody) -> Domain
    ): BuilderRepositoryResult<Domain> {
        return try {
            val response = call()
            handleResponse(response, onSuccess)
        } catch (e: Exception) {
            BuilderRepositoryResult.onError(e.message ?: GENERIC_ERROR_MESSAGE, 500)
        }
    }

    private fun <ResponseBody, Domain> handleResponse(
        response: Response<ResponseBody>,
        onSuccess: (ResponseBody) -> Domain
    ): BuilderRepositoryResult<Domain> {
        return if (response.isSuccessful) {
            handleSuccess(response, onSuccess)
        } else {
            handleFailure(response)
        }
    }

    private fun <ResponseBody, Domain> handleSuccess(
        response: Response<ResponseBody>,
        onSuccess: (ResponseBody) -> Domain
    ): BuilderRepositoryResult<Domain> {
        return response.body()?.let { responseBody ->
            BuilderRepositoryResult.onSuccess(onSuccess(responseBody))
        } ?: BuilderRepositoryResult.onError(NULL_BODY_MESSAGE, response.code())
    }

    private fun <ResponseBody, Domain> handleFailure(
        response: Response<ResponseBody>
    ): BuilderRepositoryResult<Domain> {
        val errorBodyString = response.errorBody()?.string()
        if (errorBodyString.isNullOrBlank()) {
            return BuilderRepositoryResult.onError(EMPTY_ERROR_MESSAGE, response.code())
        }

        val message = runCatching {
            val errorDto = Gson().fromJson(
                errorBodyString,
                ErrorResponseDTO::class.java
            )
            errorDto.message ?: errorDto.error ?: errorBodyString
        }.getOrDefault(errorBodyString)

        return BuilderRepositoryResult.onError(message, response.code())
    }
}
