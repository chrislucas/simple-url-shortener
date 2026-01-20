package com.br.urlshortener

interface UrlShortenerStrategy {
    fun apply(url: String): String
}