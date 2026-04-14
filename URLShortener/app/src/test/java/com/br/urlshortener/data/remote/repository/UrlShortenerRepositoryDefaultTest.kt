package com.br.urlshortener.data.remote.repository

import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.data.remote.model.LinkDTO
import com.br.urlshortener.data.remote.model.UrlResultDTO
import com.br.urlshortener.data.remote.model.UrlShortenerDTO
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.RepositoryResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class UrlShortenerRepositoryDefaultTest {

    @MockK
    private lateinit var client: UrlShortenerClient

    private lateinit var repository: UrlShortenerRepositoryDefault

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = UrlShortenerRepositoryDefault(client)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `postUrl returns success when client returns successful response`() = runTest {
        // Given
        val url = "https://example.com"
        val urlShortener = UrlShortener.createToPostUrl(url)
        val urlShortenerDTO = UrlShortenerDTO(tinyUrl = urlShortener.url)
        val urlResultDTO = UrlResultDTO(
            alias = "alias123",
            link = LinkDTO(originalUrl = urlShortener.url, tinyUrl = "https://short.url/alias123")
        )
        coEvery { client.postUrl(urlShortenerDTO) } returns Response.success(urlResultDTO)

        // When
        val result = repository.postUrl(urlShortener)

        // Then
        assertTrue(result is RepositoryResult.Success)
        val successResult = result as RepositoryResult.Success
        assertEquals("alias123", successResult.data.alias)
        assertEquals(urlShortener.url, successResult.data.link.self)
        assertEquals("https://short.url/alias123", successResult.data.link.short)
    }

    @Test
    fun `postUrl returns error when client returns unsuccessful response`() = runTest {
        // Given
        val url = "https://example.com"
        val urlShortener = UrlShortener.createToPostUrl(url)
        val urlShortenerDTO = UrlShortenerDTO(tinyUrl = urlShortener.url)
        val errorBody = "\"Error message\"".toResponseBody(null)
        coEvery { client.postUrl(urlShortenerDTO) } returns Response.error(400, errorBody)

        // When
        val result = repository.postUrl(urlShortener)

        // Then
        assertTrue(result is RepositoryResult.Error)
        val errorResult = result as RepositoryResult.Error
        assertEquals("Error message", errorResult.message)
        assertEquals(400, errorResult.code)
    }

    @Test
    fun `postUrl returns error when client returns null body`() = runTest {
        // Given
        val url = "https://example.com"
        val urlShortener = UrlShortener.createToPostUrl(url)
        val urlShortenerDTO = UrlShortenerDTO(tinyUrl = urlShortener.url)
        coEvery { client.postUrl(urlShortenerDTO) } returns Response.success(null)

        // When
        val result = repository.postUrl(urlShortener)

        // Then
        assertTrue(result is RepositoryResult.Error)
        val errorResult = result as RepositoryResult.Error
        assertEquals("null_body", errorResult.message)
    }

    @Test
    fun `postUrl returns error when client throws exception`() = runTest {
        // Given
        val url = "https://example.com"
        val urlShortener = UrlShortener.createToPostUrl(url)
        val urlShortenerDTO = UrlShortenerDTO(tinyUrl = urlShortener.url)
        coEvery { client.postUrl(urlShortenerDTO) } throws Exception("Network Error")

        // When
        val result = repository.postUrl(urlShortener)

        // Then
        assertTrue(result is RepositoryResult.Error)
        val errorResult = result as RepositoryResult.Error
        assertEquals("Network Error", errorResult.message)
        assertEquals(500, errorResult.code)
    }

    @Test
    fun `getUrlShortener returns success when client returns successful response`() = runTest {
        // Given
        val id = "alias123"
        val urlShortenerDTO = UrlShortenerDTO(tinyUrl = "https://example.com")
        coEvery { client.getUrlShortener(id) } returns Response.success(urlShortenerDTO)

        // When
        val result = repository.getUrlShortener(id)

        // Then
        assertTrue(result is RepositoryResult.Success)
        val successResult = result as RepositoryResult.Success
        assertEquals("https://example.com", successResult.data.url)
    }

    @Test
    fun `getUrlShortener returns error when client returns unsuccessful response`() = runTest {
        // Given
        val id = "alias123"
        val errorBody = "\"Not Found\"".toResponseBody(null)
        coEvery { client.getUrlShortener(id) } returns Response.error(404, errorBody)

        // When
        val result = repository.getUrlShortener(id)

        // Then
        assertTrue(result is RepositoryResult.Error)
        val errorResult = result as RepositoryResult.Error
        assertEquals("Not Found", errorResult.message)
        assertEquals(404, errorResult.code)
    }

    @Test
    fun `getUrlShortener returns error when client returns null body`() = runTest {
        // Given
        val id = "alias123"
        coEvery { client.getUrlShortener(id) } returns Response.success(null)

        // When
        val result = repository.getUrlShortener(id)

        // Then
        assertTrue(result is RepositoryResult.Error)
        val errorResult = result as RepositoryResult.Error
        assertEquals("null_body", errorResult.message)
    }

    @Test
    fun `getUrlShortener returns error when client throws exception`() = runTest {
        // Given
        val id = "alias123"
        coEvery { client.getUrlShortener(id) } throws Exception("Not Found Error")

        // When
        val result = repository.getUrlShortener(id)

        // Then
        assertTrue(result is RepositoryResult.Error)
        val errorResult = result as RepositoryResult.Error
        assertEquals("Not Found Error", errorResult.message)
        assertEquals(500, errorResult.code)
    }
}
