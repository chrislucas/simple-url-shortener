package com.br.urlshortener.domain.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuilderRepositoryResultTest {

    @Test
    fun `onSuccess should return Success with data`() {
        val data = "Test Data"
        val result = BuilderRepositoryResult.onSuccess(data)

        assertTrue(result is BuilderRepositoryResult.Success)
        assertEquals(data, (result as BuilderRepositoryResult.Success).data)
    }

    @Test
    fun `onError should return Error with message and code`() {
        val message = "Error message"
        val code = 400
        val result = BuilderRepositoryResult.onError(message, code)

        assertTrue(result is BuilderRepositoryResult.Error)
        val error = result as BuilderRepositoryResult.Error
        assertEquals(message, error.message)
        assertEquals(code, error.code)
    }

    @Test
    fun `onError should return Error with message and null code when code is not provided`() {
        val message = "Error message"
        val result = BuilderRepositoryResult.onError(message)

        assertTrue(result is BuilderRepositoryResult.Error)
        val error = result as BuilderRepositoryResult.Error
        assertEquals(message, error.message)
        assertEquals(null, error.code)
    }

    @Test
    fun `Success data class should hold data correctly`() {
        val data = 123
        val success = BuilderRepositoryResult.Success(data)

        assertEquals(data, success.data)
    }

    @Test
    fun `Error data class should hold message and code correctly`() {
        val message = "Test Error"
        val code = 500
        val error = BuilderRepositoryResult.Error(message, code)

        assertEquals(message, error.message)
        assertEquals(code, error.code)
    }
}
