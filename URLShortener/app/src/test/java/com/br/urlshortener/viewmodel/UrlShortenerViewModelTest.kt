package com.br.urlshortener.viewmodel

import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.RepositoryResult
import com.br.urlshortener.domain.repository.UrlShortenerRepository
import com.br.urlshortener.ui.event.UrlShortenerEvent
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
import org.junit.jupiter.api.assertThrows

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
        viewModel = UrlShortenerViewModel(repository, testDispatcher)
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
        val urlShortener = UrlShortener.createToPostUrl(url)
        val expectedResult = UrlResult(
            alias = "someId",
            link = Link(self = url, short = "https://sh.rt/someId")
        )

        viewModel.onChangeTextFieldContent(url)
        coEvery { repository.postUrl(urlShortener) } returns RepositoryResult.onSuccess(expectedResult)

        val events = mutableListOf<UrlShortenerEvent>()
        val job = launch { viewModel.navigationEvent.collect { events.add(it) } }

        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify { repository.postUrl(urlShortener) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Idle)
        assertEquals(1, viewModel.urls.value.size)
        assertTrue(viewModel.urls.value.contains(expectedResult))
        
        assertEquals(1, events.size)
        assertTrue(events[0] is UrlShortenerEvent.ShowSnackBar)
        assertEquals("URL encurtada com sucesso", (events[0] as UrlShortenerEvent.ShowSnackBar).message)
        
        job.cancel()
    }

    @Test
    fun `postAction PostShortUrlEvent with invalid URL format`() = runTest {
        val invalidUrl = "not-a-valid-url"
        viewModel.onChangeTextFieldContent(invalidUrl)

        val events = mutableListOf<UrlShortenerEvent>()
        val job = launch { viewModel.navigationEvent.collect { events.add(it) } }

        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.postUrl(any<UrlShortener>()) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Idle)
        
        assertEquals(1, events.size)
        assertTrue(events[0] is UrlShortenerEvent.ShowError)
        assertEquals("Invalid URL format: $invalidUrl", (events[0] as UrlShortenerEvent.ShowError).message)
        
        job.cancel()
    }

    @Test
    fun `postAction PostShortUrlEvent with empty URL`() = runTest {
        viewModel.onChangeTextFieldContent("")

        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)

        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.postUrl(any<UrlShortener>()) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Idle)
    }

    @Test
    fun `postAction PostShortUrlEvent when repository returns error`() = runTest {
        val url = "https://google.com"
        val urlShortener = UrlShortener.createToPostUrl(url)

        viewModel.onChangeTextFieldContent(url)
        coEvery { repository.postUrl(urlShortener) } returns RepositoryResult.onError("Server Error", 500)

        val events = mutableListOf<UrlShortenerEvent>()
        val job = launch { viewModel.navigationEvent.collect { events.add(it) } }

        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Loading)
        advanceUntilIdle()

        coVerify { repository.postUrl(urlShortener) }
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Idle)
        assertTrue(viewModel.urls.value.isEmpty())

        assertEquals(1, events.size)
        assertTrue(events[0] is UrlShortenerEvent.ShowError)
        val errorEvent = events[0] as UrlShortenerEvent.ShowError
        assertTrue(errorEvent.message.contains("Server Error"))
        assertTrue(errorEvent.message.contains("500"))

        job.cancel()
    }

    @Test(expected = RuntimeException::class)
    fun `postAction PostShortUrlEvent when repository throws exception`() = runTest {
        val url = "https://google.com"
        viewModel.onChangeTextFieldContent(url)
        coEvery { repository.postUrl(any<UrlShortener>()) } answers {
            throw RuntimeException("Network error")
        }

        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)
        advanceUntilIdle()
    }

    @Test
    fun `postAction GetShortUrlEvent triggers success flow`() = runTest {
        val id = "someId"
        val expectedResult = UrlShortener.createFromGetResult(
            url = "https://sh.rt/$id"
        )
        coEvery { repository.getUrlShortener(id) } returns RepositoryResult.onSuccess(expectedResult)

        val events = mutableListOf<UrlShortenerEvent>()
        val job = launch { viewModel.navigationEvent.collect { events.add(it) } }

        viewModel.uiEventInterpreter(UrlShortenerUIEvent.GetShortUrlEvent(id))
        advanceUntilIdle()

        coVerify { repository.getUrlShortener(id) }
        assertEquals(expectedResult, viewModel.urlShortener.value)
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Idle)
        
        assertEquals(1, events.size)
        assertTrue(events[0] is UrlShortenerEvent.NavigateToDetail)

        job.cancel()
    }

    @Test
    fun `postAction multiple PostShortUrlEvent calls`() = runTest {
        val url1 = "https://google.com"
        val url2 = "https://youtube.com"
        val result1 = UrlResult(alias = "1", link = Link(self = url1, short = "short1"))
        val result2 = UrlResult(alias = "2", link = Link(self = url2, short = "short2"))

        // First call
        viewModel.onChangeTextFieldContent(url1)
        coEvery { repository.postUrl(UrlShortener.createToPostUrl(url1)) } returns RepositoryResult.onSuccess(result1)
        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)
        advanceUntilIdle()

        // Second call
        viewModel.onChangeTextFieldContent(url2)
        coEvery { repository.postUrl(UrlShortener.createToPostUrl(url2)) } returns RepositoryResult.onSuccess(result2)
        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)
        advanceUntilIdle()

        assertEquals(2, viewModel.urls.value.size)
        assertTrue(viewModel.urls.value.contains(result1))
        assertTrue(viewModel.urls.value.contains(result2))
    }

    @Test
    fun `postAction concurrent PostShortUrlEvent calls`() = runTest {
        val url1 = "https://google.com"
        val url2 = "https://youtube.com"
        val result1 = UrlResult(alias = "1", link = Link(self = url1, short = "short1"))
        val result2 = UrlResult(alias = "2", link = Link(self = url2, short = "short2"))

        coEvery { repository.postUrl(UrlShortener.createToPostUrl(url1)) } returns RepositoryResult.onSuccess(result1)
        coEvery { repository.postUrl(UrlShortener.createToPostUrl(url2)) } returns RepositoryResult.onSuccess(result2)

        viewModel.onChangeTextFieldContent(url1)
        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)

        viewModel.onChangeTextFieldContent(url2)
        viewModel.uiEventInterpreter(UrlShortenerUIEvent.PostShortUrlEvent)

        advanceUntilIdle()

        val urls = viewModel.urls.value
        assertEquals(2, urls.size)
        assertTrue(urls.contains(result1))
        assertTrue(urls.contains(result2))
    }

    @Test
    fun `putUiOnIdle resets state`() = runTest {
        viewModel.onChangeTextFieldContent("some text")
        viewModel.putUiOnIdle()
        
        assertTrue(viewModel.uiState.value is UrlShortenerUIState.Idle)
        assertNull(viewModel.urlShortener.value)
    }
}
