package com.br.urlshortener.domain.repository

import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.data.remote.model.UrlShortenerDTO
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener

class UrlShortenerRepositoryDefault(private val client: UrlShortenerClient) : UrlShortenerRepository {

    override suspend fun postUrl(urlShortener: UrlShortener): RepositoryResult<UrlResult> {
        val call = suspend { client.postUrl(UrlShortenerDTO(urlShortener.url)) }
        return SafeRepository.remoteCall(call) { responseBody ->
            responseBody.let {
                UrlResult(
                    alias = it.alias,
                    link = Link(
                        self = it.link.originalUrl,
                        short = it.link.tinyUrl
                    )
                )
            }
        }
    }

    override suspend fun getUrlShortener(id: String): RepositoryResult<UrlShortener> {
        val call = suspend { client.getUrlShortener(id) }
        return SafeRepository.remoteCall(call) { responseBody ->
            UrlShortener.createFromGetResult(responseBody.tinyUrl)
        }
    }
}
