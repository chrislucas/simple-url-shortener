package com.br.urlshortener.data.remote.model

import com.google.gson.annotations.SerializedName

data class ErrorResponseDTO(
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)
