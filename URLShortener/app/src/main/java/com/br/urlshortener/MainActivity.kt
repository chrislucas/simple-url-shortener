package com.br.urlshortener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.ui.component.ErrorOverlayComponent
import com.br.urlshortener.ui.component.LoadingOverlayComponent
import com.br.urlshortener.ui.component.UrlShortenerFormComponent
import com.br.urlshortener.ui.component.UrlShortenerListComponent
import com.br.urlshortener.ui.screen.UrlDetailScreen
import com.br.urlshortener.ui.screen.UrlShortenerApp
import com.br.urlshortener.ui.state.UrlShortenerUIState
import com.br.urlshortener.ui.theme.URLShortenerTheme
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            URLShortenerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UrlShortenerScreenApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
internal fun UrlShortenerScreenApp(modifier: Modifier = Modifier) {
    UrlShortenerApp(modifier = modifier)
}


@Deprecated("Use UrlShortenerInitApp instead")
@Composable
internal fun UrlShortenerScreen(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(
        factory = UrlShortenerViewModel.FACTORY
    )
) {
    val uiState = urlShortenerViewModel.uiState.collectAsState()
    when (uiState.value) {
        is UrlShortenerUIState.Loading -> {
            // You can add a loading indicator here
            LoadingOverlayComponent()
        }

        is UrlShortenerUIState.Success<*> -> {
            val s = (uiState.value as UrlShortenerUIState.Success<*>).data
            if (s is UrlShortener) {
                UrlDetailScreen(s.url)
            } else {
                UrlShortenerForm(modifier, urlShortenerViewModel)
            }
        }

        is UrlShortenerUIState.Error -> {
            // You can show an error message here
            ErrorOverlayComponent()
        }

        else -> {
            UrlShortenerForm(modifier, urlShortenerViewModel)
        }
    }
}


@Composable
private fun UrlShortenerForm(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(factory = UrlShortenerViewModel.FACTORY)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .systemBarsPadding()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UrlShortenerFormComponent(modifier, urlShortenerViewModel)
        UrlShortenerListComponent(modifier, urlShortenerViewModel)
    }
}
