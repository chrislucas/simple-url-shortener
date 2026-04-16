package com.br.urlshortener.domain.repository

import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class SafeRepositoryTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given successful call and body when remoteCall then return success`() = runTest {
        // Given
        val responseBody = "Success Body"
        val call = suspend { Response.success(responseBody) }
        val onSuccess: (String) -> Int = { it.length }

        // When
        val result = SafeRepository.remoteCall(call, onSuccess)

        // Then
        assertTrue(result is BuilderRepositoryResult.Success)
        assertEquals(responseBody.length, (result as BuilderRepositoryResult.Success).data)
    }

    @Test
    fun `given successful call and null body when remoteCall then return error with null body message`() = runTest {
        // Given
        val call = suspend { Response.success<String>(null) }
        val onSuccess: (String) -> Int = { it.length }

        // When
        val result = SafeRepository.remoteCall(call, onSuccess)

        // Then
        assertTrue(result is BuilderRepositoryResult.Error)
        val errorResult = result as BuilderRepositoryResult.Error
        assertEquals("null_body", errorResult.message)
        assertEquals(200, errorResult.code)
    }

    @Test
    fun `given unsuccessful call when remoteCall then return error with message`() = runTest {
        // Given
        val errorCode = 404
        val errorMessage = "Not Found"
        // The implementation uses Gson().fromJson(..., ErrorResponseDTO::class.java)
        val errorJson = "{\"message\": \"$errorMessage\"}"
        val response = Response.error<String>(
            errorCode,
            errorJson.toResponseBody("application/json".toResponseBody().contentType())
        )
        val call = suspend { response }
        val onSuccess: (String) -> Int = { it.length }

        // When
        val result = SafeRepository.remoteCall(call, onSuccess)

        // Then
        assertTrue(result is BuilderRepositoryResult.Error)
        val errorResult = result as BuilderRepositoryResult.Error
        assertEquals(errorMessage, errorResult.message)
        assertEquals(errorCode, errorResult.code)
    }

    @Test
    fun `given unsuccessful call and fallback message when remoteCall then return error with fallback`() = runTest {
        // Given
        val errorCode = 400
        val errorContent = "{\"error\": \"Bad Request\"}"
        val response = Response.error<String>(errorCode, errorContent.toResponseBody(null))
        val call = suspend { response }
        val onSuccess: (String) -> Int = { it.length }

        // When
        val result = SafeRepository.remoteCall(call, onSuccess)

        // Then
        assertTrue(result is BuilderRepositoryResult.Error)
        val errorResult = result as BuilderRepositoryResult.Error
        assertEquals("Bad Request", errorResult.message)
        assertEquals(errorCode, errorResult.code)
    }

    @Test
    fun `given unsuccessful call with no JSON when remoteCall then return error with raw body`() = runTest {
        // Given
        val errorCode = 400
        val rawBody = "Something went wrong"
        val response = Response.error<String>(errorCode, rawBody.toResponseBody(null))
        val call = suspend { response }
        val onSuccess: (String) -> Int = { it.length }

        // When
        val result = SafeRepository.remoteCall(call, onSuccess)

        // Then
        assertTrue(result is BuilderRepositoryResult.Error)
        val errorResult = result as BuilderRepositoryResult.Error
        assertEquals(rawBody, errorResult.message)
        assertEquals(errorCode, errorResult.code)
    }

    @Test
    fun `given call throwing exception when remoteCall then return error 500`() = runTest {
        // Given
        val exceptionMessage = "Network Failure"
        val call = suspend { throw Exception(exceptionMessage) }
        val onSuccess: (String) -> Int = { it.length }

        // When
        val result = SafeRepository.remoteCall(call, onSuccess)

        // Then
        assertTrue(result is BuilderRepositoryResult.Error)
        val errorResult = result as BuilderRepositoryResult.Error
        assertEquals(exceptionMessage, errorResult.message)
        assertEquals(500, errorResult.code)
    }
}
