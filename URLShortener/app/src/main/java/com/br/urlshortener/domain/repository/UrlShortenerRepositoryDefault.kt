package com.br.urlshortener.domain.repository

import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.data.remote.model.UrlShortenerDTO
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener

class UrlShortenerRepositoryDefault(private val client: UrlShortenerClient) : UrlShortenerRepository {

    override suspend fun postUrl(urlShortener: UrlShortener): UrlResult? {
        val response = client.postUrl(UrlShortenerDTO(urlShortener.tinyUrl))
        return if (response.isSuccessful) {
            // Handle successful response if needed
            UrlResult(
                alias = "79673226",
                link = Link(
                    self = "https://api.short.io/links/79673226",
                    short = "https://short.io/79673226"
                )
            )
        } else {
            // Handle error response if needed
            null
        }
    }

    override suspend fun getUrlShortener(id: String): UrlResult {
        return UrlResult(
            alias = "79673226",
            link = Link(
                self = "https://api.short.io/links/79673226",
                short = "https://short.io/79673226"
            )
        )
    }
}
