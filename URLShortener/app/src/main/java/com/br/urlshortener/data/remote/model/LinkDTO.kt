package com.br.urlshortener.data.remote.model

import com.google.gson.annotations.SerializedName

data class LinkDTO(
    @SerializedName("self") val originalUrl: String,
    @SerializedName("short") val tinyUrl: String
)
