package com.br.urlshortener.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.UrlShortenerRepository
import com.br.urlshortener.ui.component.ErrorOverlayComponent
import com.br.urlshortener.ui.component.LoadingOverlayComponent
import com.br.urlshortener.ui.component.UrlShortenerFormComponent
import com.br.urlshortener.ui.component.UrlShortenerListComponent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import com.br.urlshortener.ui.theme.URLShortenerTheme
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

@Composable
internal fun UrlShortenerScreen(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel,
    onClickItem: () -> Unit = {},
    onBackPressed: () -> Unit,
) {
    val uiState by urlShortenerViewModel.uiState.collectAsState()
    when (uiState) {
        is UrlShortenerUIState.Loading -> {
            LoadingOverlayComponent()
            UrlShortenerForm(modifier, urlShortenerViewModel, onClickItem)
            urlShortenerViewModel.putUiOnIdle()
        }

        is UrlShortenerUIState.Error -> {
            val errorMessage = (uiState as UrlShortenerUIState.Error).message
            ErrorOverlayComponent(errorMessage)
            UrlShortenerForm(modifier, urlShortenerViewModel, onClickItem)
            urlShortenerViewModel.putUiOnIdle()
        }

        is UrlShortenerUIState.Success<*> -> {
            val s = (uiState as UrlShortenerUIState.Success<*>).data
            if (s is UrlShortener && !s.url.isEmpty()) {
                UrlDetailScreen(s.url, onBackPressed)
            } else {
                UrlShortenerForm(modifier, urlShortenerViewModel, onClickItem)
            }
        }

        else -> {
            UrlShortenerForm(modifier, urlShortenerViewModel, onClickItem)
        }
    }
}

@Composable
private fun UrlShortenerForm(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(factory = UrlShortenerViewModel.FACTORY),
    onClickItem: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Top
    ) {
        UrlShortenerFormComponent(
            modifier = Modifier,
            urlShortenerViewModel = urlShortenerViewModel
        )
        UrlShortenerListComponent(
            modifier = Modifier.padding(top = 3.dp, bottom = 3.dp),
            urlShortenerViewModel = urlShortenerViewModel,
            onClickItem = onClickItem
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UrlShortenerFormPreview() {
    val previewViewModel = remember {
        UrlShortenerViewModel(
            repository = object : UrlShortenerRepository {
                override suspend fun postUrl(urlShortener: UrlShortener): UrlResult? = null

                override suspend fun getUrlShortener(id: String): UrlShortener? = null
            }
        )
    }

    URLShortenerTheme {
        UrlShortenerForm(urlShortenerViewModel = previewViewModel)
    }
}
