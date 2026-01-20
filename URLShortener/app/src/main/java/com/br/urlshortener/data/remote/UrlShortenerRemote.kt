package com.br.urlshortener.data.remote

import com.br.urlshortener.UrlShortenerStrategy
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.data.remote.model.UrlShortenerDTO
import com.br.urlshortener.domain.repository.UrlShortenerRepository

class UrlShortenerRemoteRepository(
    private val urlShortenerStrategy: UrlShortenerStrategy,
    private val client: UrlShortenerClient,
) : UrlShortenerRepository {

    override suspend fun postUrl(url: String) {

        val urlShortener = UrlShortenerDTO(urlShortenerStrategy.apply(url))
        client.postUrl(urlShortener)
    }

    override suspend fun getUrlShortener(id: String): UrlShortener {
        client.getUrlShortener(id)
        return UrlShortener.create("https://short.io/$id")
    }
}