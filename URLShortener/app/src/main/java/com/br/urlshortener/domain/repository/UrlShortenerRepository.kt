package com.br.urlshortener.domain.repository

import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener

interface UrlShortenerRepository {

    suspend fun postUrl(urlShortener: UrlShortener): BuilderRepositoryResult<UrlResult>

    suspend fun getUrlShortener(id: String): BuilderRepositoryResult<UrlShortener>
}
