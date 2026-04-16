package com.br.urlshortener.ui.state

import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener

data class UrlShortenerScreenState(
    val textFieldContent: String = "",
    val urls: Set<UrlResult> = emptySet(),
    val uiStatus: UrlShortenerUIState = UrlShortenerUIState.Idle,
    val selectedDetail: UrlShortener? = null,
)
