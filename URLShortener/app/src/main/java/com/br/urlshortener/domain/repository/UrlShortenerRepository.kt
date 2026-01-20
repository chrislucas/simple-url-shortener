package com.br.urlshortener.domain.repository

import com.br.urlshortener.domain.model.UrlShortener

interface UrlShortenerRepository {

    suspend fun postUrl(url: String)

    suspend fun getUrlShortener(id: String): UrlShortener
}