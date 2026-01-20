package com.br.urlshortener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
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
                    UrlShortenerScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

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
        }

        is UrlShortenerUIState.Error -> {
            // You can show an error message here
        }

        else -> {
            // Idle or Success state, no special UI needed
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
        UrlShortenerListComponent(modifier, urlShortenerViewModel)
    }
}
