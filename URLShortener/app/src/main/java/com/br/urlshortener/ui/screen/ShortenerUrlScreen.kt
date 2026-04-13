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
import com.br.urlshortener.domain.repository.RepositoryResult
import com.br.urlshortener.domain.repository.UrlShortenerRepository
import com.br.urlshortener.ui.component.OverlayErrorComponent
import com.br.urlshortener.ui.component.LoadingOverlayComponent
import com.br.urlshortener.ui.component.UrlShortenerFormComponent
import com.br.urlshortener.ui.component.UrlShortenerListComponent
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import com.br.urlshortener.ui.theme.URLShortenerTheme
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

@Composable
internal fun UrlShortenerScreen(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel,
) {
    val uiState by urlShortenerViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UrlShortenerUIState.Loading -> {
            LoadingOverlayComponent()
        }

        is UrlShortenerUIState.Error -> {
            if (state.message.isNotBlank()) {
                OverlayErrorComponent(state.message)
            }
        }

        else -> {
            // Idle ou Success não requerem overlays específicos aqui
            // Nothing
        }
    }
    UrlShortenerForm(modifier, urlShortenerViewModel)
}

@Composable
private fun UrlShortenerForm(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(factory = UrlShortenerViewModel.FACTORY),
) {
    val urls by urlShortenerViewModel.urls.collectAsState()

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
            urls = urls.toList(),
            onClickItem = { pathId ->
                urlShortenerViewModel.uiEventInterpreter(UrlShortenerUIEvent.GetShortUrlEvent(pathId))
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UrlShortenerFormPreview() {
    val previewViewModel = remember {
        UrlShortenerViewModel(
            repository = object : UrlShortenerRepository {

                override suspend fun postUrl(urlShortener: UrlShortener): RepositoryResult<UrlResult> =
                    RepositoryResult.onError("Any Error")

                override suspend fun getUrlShortener(id: String): RepositoryResult<UrlShortener> =
                    RepositoryResult.onError("Any Error")
            }
        )
    }

    URLShortenerTheme {
        UrlShortenerForm(
            urlShortenerViewModel = previewViewModel,
        )
    }
}
