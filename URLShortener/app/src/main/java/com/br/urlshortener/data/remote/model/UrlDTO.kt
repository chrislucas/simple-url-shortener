package com.br.urlshortener.data.remote.model

import com.google.gson.annotations.SerializedName

data class UrlDTO(
    val alias: String,
    @SerializedName("_links") val link: LinkDTO
)