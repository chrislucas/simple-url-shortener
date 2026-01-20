package com.br.urlshortener.ui.event

sealed class UrlShortenerUIEvent {
    object PostShortUrlEvent : UrlShortenerUIEvent()
    data class GetShortUrlEvent(val id: String) : UrlShortenerUIEvent()
}
