package com.br.urlshortener.ui.state

import com.br.urlshortener.domain.model.UrlResult

sealed class UrlShortenerUIState {
    object Idle : UrlShortenerUIState()
    object Loading : UrlShortenerUIState()
    data class Success(val urlResult: UrlResult) : UrlShortenerUIState()
    data class Error(val message: String) : UrlShortenerUIState()
}
