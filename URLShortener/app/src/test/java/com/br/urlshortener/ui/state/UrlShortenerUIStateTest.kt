package com.br.urlshortener.ui.state

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class UrlShortenerUIStateTest {

    interface DummyData

    @Test
    fun `idle state should be singleton`() {
        val first = UrlShortenerUIState.Idle
        val second = UrlShortenerUIState.Idle

        assertSame(first, second)
    }

    @Test
    fun `loading state should be singleton`() {
        val first = UrlShortenerUIState.Loading
        val second = UrlShortenerUIState.Loading

        assertSame(first, second)
    }

    @Test
    fun `success state should keep data reference`() {
        val data = mockk<DummyData>()

        val state = UrlShortenerUIState.Success(data)

        assertSame(data, state.data)
    }

    @Test
    fun `error state should keep message`() {
        val message = "Network error"

        val state = UrlShortenerUIState.Error(message)

        assertEquals(message, state.message)
    }
}
