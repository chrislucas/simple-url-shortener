package com.br.urlshortener.domain.repository

import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.model.UrlResult

interface UrlShortenerRepository {

    suspend fun postUrl(urlShortener: UrlShortener): UrlResult?

    suspend fun getUrlShortener(id: String): UrlResult
}