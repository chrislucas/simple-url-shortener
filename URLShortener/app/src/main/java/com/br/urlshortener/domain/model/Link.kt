package com.br.urlshortener.domain.model

import com.google.gson.annotations.SerializedName

data class Link(
    val self: String,
    val short: String
)
