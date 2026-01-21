package com.br.urlshortener.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UrlShortenerTest {

    @Test
    fun `create with https url returns UrlShortener with same tinyUrl`() {
        val url = "https://example.com/path?query=1#frag"

        val result = UrlShortener.create(url)

        assertEquals(url, result.url)
    }

    @Test
    fun `create with http url returns UrlShortener with same tinyUrl`() {
        val url = "http://example.com"

        val result = UrlShortener.create(url)

        assertEquals(url, result.url)
    }

    @Test
    fun `create with ftp url returns UrlShortener with same tinyUrl`() {
        val url = "ftp://example.com/resource"

        val result = UrlShortener.create(url)

        assertEquals(url, result.url)
    }

    @Test
    fun `create throws when url is empty`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            UrlShortener.create("")
        }

        assertEquals("Invalid URL format", exception.message)
    }

    @Test
    fun `create throws when url has no scheme`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            UrlShortener.create("www.example.com")
        }

        assertEquals("Invalid URL format", exception.message)
    }

    @Test
    fun `create throws when url contains spaces`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            UrlShortener.create("https://example.com/invalid path")
        }

        assertEquals("Invalid URL format", exception.message)
    }
}