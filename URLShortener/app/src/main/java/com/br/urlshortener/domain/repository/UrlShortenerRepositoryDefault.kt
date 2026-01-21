package com.br.urlshortener.domain.repository

import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.data.remote.model.UrlShortenerDTO
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener

class UrlShortenerRepositoryDefault(private val client: UrlShortenerClient) : UrlShortenerRepository {

    override suspend fun postUrl(urlShortener: UrlShortener): UrlResult? {
        val response = client.postUrl(UrlShortenerDTO(urlShortener.url))
        return if (response.isSuccessful) {
            val result = response.body()
            // Handle successful response if needed
            result?.let {
                UrlResult(
                    alias = it.alias,
                    link = Link(
                        self = it.link.originalUrl,
                        short = it.link.tinyUrl
                    )
                )
            }
        } else {
            // Handle error response if needed
            null
        }
    }

    override suspend fun getUrlShortener(id: String): UrlShortener? {
        val response = client.getUrlShortener(id)
        return if (response.isSuccessful) {
            val result = response.body()
            result?.let { urlShortener ->
                UrlShortener.createFromGetResult(urlShortener.tinyUrl)
            }
        } else {
            null
        }
    }
}
