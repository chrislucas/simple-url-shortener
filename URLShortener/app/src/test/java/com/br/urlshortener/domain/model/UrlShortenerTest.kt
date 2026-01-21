package com.br.urlshortener.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.net.URL

class UrlShortenerTest {

    @Test
    fun `create with valid url returns shortened url with 8 char domain and same scheme`() {
        val url = "https://example.com/path?query=1#frag"

        val result = UrlShortener.createToPostUrl(url)
        val shortenedUrl = URL(result.url)
        val domainLabel = shortenedUrl.host.substringBefore(".")

        assertEquals("https", shortenedUrl.protocol)
        assertEquals(8, domainLabel.length)
        assertNotEquals(url, result.url)
    }

    @Test
    fun `create with ftp url keeps ftp scheme`() {
        val url = "ftp://example.com/resource"

        val result = UrlShortener.createToPostUrl(url)
        val shortenedUrl = URL(result.url)

        assertEquals("ftp", shortenedUrl.protocol)
    }

    @Test
    fun `create returns deterministic shortened url for same input`() {
        val url = "https://example.com/resource"

        val first = UrlShortener.createToPostUrl(url).url
        val second = UrlShortener.createToPostUrl(url).url

        assertEquals(first, second)
    }

    @Test
    fun `create throws when url is empty`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            UrlShortener.createToPostUrl("")
        }

        assertEquals("Invalid URL format", exception.message)
    }

    @Test
    fun `create throws when url has no scheme`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            UrlShortener.createToPostUrl("www.example.com")
        }

        assertEquals("Invalid URL format", exception.message)
    }

    @Test
    fun `create throws when url contains spaces`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            UrlShortener.createToPostUrl("https://example.com/invalid path")
        }

        assertEquals("Invalid URL format", exception.message)
    }
}
