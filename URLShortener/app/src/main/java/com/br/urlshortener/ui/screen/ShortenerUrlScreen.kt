package com.br.urlshortener.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.br.urlshortener.ui.component.ErrorOverlayComponent
import com.br.urlshortener.ui.component.LoadingOverlayComponent
import com.br.urlshortener.ui.component.UrlShortenerFormComponent
import com.br.urlshortener.ui.component.UrlShortenerListComponent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

@Composable
internal fun UrlShortenerScreen(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel,
    onClickItem: () -> Unit = {}
) {
    val uiState = urlShortenerViewModel.uiState.collectAsState()
    when (uiState.value) {
        is UrlShortenerUIState.Loading -> {
            // You can add a loading indicator here
            LoadingOverlayComponent()
        }

        is UrlShortenerUIState.Error -> {
            // You can show an error message here
            ErrorOverlayComponent()
        }

        else -> {
            // Idle or Success state, no special UI needed - DO NOTHING
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .systemBarsPadding()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UrlShortenerFormComponent(modifier, urlShortenerViewModel)
        UrlShortenerListComponent(modifier, urlShortenerViewModel, onClickItem)
    }
}