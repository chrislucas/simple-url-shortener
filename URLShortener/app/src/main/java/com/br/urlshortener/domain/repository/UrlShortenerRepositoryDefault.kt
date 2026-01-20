package com.br.urlshortener.domain.repository

import com.br.urlshortener.UrlShortenerScreen
import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.domain.model.UrlShortener

class UrlShortenerRepositoryDefault(
    // private val client: UrlShortenerClient
): UrlShortenerRepository {

    override suspend fun postUrl(url: String) {

    }

    override suspend fun getUrlShortener(id: String): UrlShortener {
       return UrlShortener.create("https://short.io/")
    }
}