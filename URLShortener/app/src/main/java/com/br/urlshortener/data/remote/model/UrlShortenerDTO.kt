package com.br.urlshortener.data.remote.model

import com.google.gson.annotations.SerializedName

data class UrlShortenerDTO(@SerializedName("url") val tinyUrl: String)
