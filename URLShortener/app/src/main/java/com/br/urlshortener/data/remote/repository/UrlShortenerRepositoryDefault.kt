package com.br.urlshortener.data.remote.repository

import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.data.remote.model.UrlShortenerDTO
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.RepositoryResult
import com.br.urlshortener.domain.repository.SafeRepository
import com.br.urlshortener.domain.repository.UrlShortenerRepository

class UrlShortenerRepositoryDefault(private val client: UrlShortenerClient) : UrlShortenerRepository {

    override suspend fun postUrl(urlShortener: UrlShortener): RepositoryResult<UrlResult> {
        val call = suspend { client.postUrl(UrlShortenerDTO(urlShortener.url)) }
        return SafeRepository.remoteCall(call) { responseBody ->
            UrlResult(
                alias = responseBody.alias,
                link = Link(
                    self = responseBody.link.originalUrl,
                    short = responseBody.link.tinyUrl
                )
            )
        }
    }

    override suspend fun getUrlShortener(id: String): RepositoryResult<UrlShortener> {
        val call = suspend { client.getUrlShortener(id) }
        return SafeRepository.remoteCall(call) { responseBody ->
            UrlShortener.createFromGetResult(responseBody.tinyUrl)
        }
    }
}
