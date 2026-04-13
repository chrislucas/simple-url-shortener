package com.br.urlshortener.ui.event

/**
 * Eventos one-shot: consumidos uma única vez pela UI.
 * Usados para navegação, Snackbar e outras ações efêmeras.
 */
sealed class UrlShortenerEvent {
    data object NavigateToDetail : UrlShortenerEvent()

    data class ShowSnackBar(val message: String) : UrlShortenerEvent()

    data class ShowError(val message: String) : UrlShortenerEvent()
}
