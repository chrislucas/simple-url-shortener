package com.br.urlshortener.viewmodel

import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.UrlShortenerRepository
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UrlShortenerViewModelTest {
    @RelaxedMockK
    private lateinit var repository: UrlShortenerRepository

    private lateinit var viewModel: UrlShortenerViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = UrlShortenerViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getTextFieldContent initial state`() = runTest {
        assertEquals("", viewModel.textFieldContent.value)
    }

    @Test
    fun `getUrls initial state`() = runTest {
        assertTrue(viewModel.urls.value.isEmpty())
    }

    @Test
    fun `getUiState initial state`() = runTest {
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Idle)
    }

    @Test
    fun `onChangeTextFieldContent updates state`() = runTest {
        val newText = "https://example.com"
        viewModel.onChangeTextFieldContent(newText)
        advanceUntilIdle()
        assertEquals(newText, viewModel.textFieldContent.value)
    }

    @Test
    fun `onChangeTextFieldContent with empty string`() = runTest {
        viewModel.onChangeTextFieldContent("")
        advanceUntilIdle()
        assertEquals("", viewModel.textFieldContent.value)
    }

    @Test
    fun `onChangeTextFieldContent with long string`() = runTest {
        val longText = "a".repeat(1000)
        viewModel.onChangeTextFieldContent(longText)
        advanceUntilIdle()
        assertEquals(longText, viewModel.textFieldContent.value)
    }

    @Test
    fun `postAction PostShortUrlEvent with valid URL`() = runTest {
        val url = "https://google.com"
        val urlShortener = UrlShortener.create(url)
        val expectedResult = UrlResult(
            alias = "someId",
            link = Link(self = url, short = "https://sh.rt/someId")
        )

        viewModel.onChangeTextFieldContent(url)
        coEvery { repository.postUrl(any<UrlShortener>()) } returns expectedResult

        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)

        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify { repository.postUrl(urlShortener) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Success)
        assertEquals(expectedResult, (viewModel.uiState.value as UrlShortenerUIState.Success).urlResult)
        assertEquals(1, viewModel.urls.value.size)
        assertEquals(expectedResult, viewModel.urls.value.first())
    }

    @Test
    fun `postAction PostShortUrlEvent with invalid URL format`() = runTest {
        val invalidUrl = "not-a-valid-url"
        viewModel.onChangeTextFieldContent(invalidUrl)

        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)

        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.postUrl(any<UrlShortener>()) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Error)
        assertEquals(
            "Invalid URL format",
            (viewModel.uiState.value as UrlShortenerUIState.Error).message
        )
    }

    @Test
    fun `postAction PostShortUrlEvent with empty URL`() = runTest {
        viewModel.onChangeTextFieldContent("")

        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)

        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.postUrl(any<UrlShortener>()) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Error)
        assertEquals(
            "Invalid URL format",
            (viewModel.uiState.value as UrlShortenerUIState.Error).message
        )
    }

    @Test
    fun `postAction PostShortUrlEvent when repository returns null`() = runTest {
        val url = "https://google.com"
        val urlShortener = UrlShortener.create(url)

        viewModel.onChangeTextFieldContent(url)
        coEvery { repository.postUrl(any<UrlShortener>()) } returns null

        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)

        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify { repository.postUrl(urlShortener) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Error)
        assertEquals(
            "Failed to post shorten URL",
            (viewModel.uiState.value as UrlShortenerUIState.Error).message
        )
        assertTrue(viewModel.urls.value.isEmpty())
    }

    @Test
    fun `postAction PostShortUrlEvent when repository throws exception`() = runTest {
        val url = "https://google.com"
        val exception = RuntimeException("Network error")
        viewModel.onChangeTextFieldContent(url)
        coEvery { repository.postUrl(any<UrlShortener>()) } throws exception

        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)

        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Error)
        assertEquals(
            exception.message,
            (viewModel.uiState.value as UrlShortenerUIState.Error).message
        )
    }

    @Test
    fun `postAction GetShortUrlEvent triggers repository call`() = runTest {
        val id = "someId"
        val expectedResult = UrlResult(
            alias = id,
            link = Link(self = "https://original.url", short = "https://sh.rt/someId")
        )
        coEvery { repository.getUrlShortener(id) } returns expectedResult

        viewModel.postAction(UrlShortenerUIEvent.GetShortUrlEvent(id))
        advanceUntilIdle()

        coVerify { repository.getUrlShortener(id) }
    }

    @Test
    fun `postAction multiple PostShortUrlEvent calls`() = runTest {
        val url1 = "https://google.com"
        val url2 = "https://youtube.com"
        val result1 = UrlResult(alias = "1", link = Link(self = url1, short = "short1"))
        val result2 = UrlResult(alias = "2", link = Link(self = url2, short = "short2"))

        // First call
        viewModel.onChangeTextFieldContent(url1)
        coEvery { repository.postUrl(UrlShortener.create(url1)) } returns result1
        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)
        advanceUntilIdle()


        // Second call
        viewModel.onChangeTextFieldContent(url2)
        coEvery { repository.postUrl(UrlShortener.create(url2)) } returns result2
        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)
        advanceUntilIdle()

        assertEquals(2, viewModel.urls.value.size)
        assertEquals(result2, viewModel.urls.value[0])
        assertEquals(result1, viewModel.urls.value[1])
    }

    @Test
    fun `postAction concurrent PostShortUrlEvent calls`() = runTest {
        val url1 = "https://google.com"
        val url2 = "https://youtube.com"
        val result1 = UrlResult(alias = "1", link = Link(self = url1, short = "short1"))
        val result2 = UrlResult(alias = "2", link = Link(self = url2, short = "short2"))

        coEvery { repository.postUrl(UrlShortener.create(url1)) } returns result1
        coEvery { repository.postUrl(UrlShortener.create(url2)) } returns result2

        viewModel.onChangeTextFieldContent(url1)
        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)

        viewModel.onChangeTextFieldContent(url2)
        viewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)

        advanceUntilIdle()

        val urls = viewModel.urls.value
        assertEquals(2, urls.size)
        assertTrue(urls.contains(result1))
        assertTrue(urls.contains(result2))
    }

    @Test
    fun `postAction GetShortUrlEvent with empty ID`() = runTest {
        val id = ""
        val exception = RuntimeException("Not found")
        coEvery { repository.getUrlShortener(id) } throws exception

        viewModel.postAction(UrlShortenerUIEvent.GetShortUrlEvent(id))
        advanceUntilIdle()

        coVerify { repository.getUrlShortener(id) }
    }
}
