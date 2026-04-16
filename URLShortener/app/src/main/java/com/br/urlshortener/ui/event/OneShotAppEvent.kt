package com.br.urlshortener.ui.event

/**
 * Eventos one-shot: consumidos uma única vez pela UI.
 * Usados para navegação, Snackbar e outras ações efêmeras.
 */
sealed class OneShotAppEvent {
    data object NavigateToDetail : OneShotAppEvent()

    data class ShowSnackBar(val message: String) : OneShotAppEvent()

    data class ShowError(val message: String) : OneShotAppEvent()
}
