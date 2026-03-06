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


/*
    TODO
    -> Criar um componente que
        - Mostra a ultima URL que o usuario enviou
        - Mostra um botão que leva para uma tela com a lista de URL
    -> Criar um componente para exibir todas as URLS
 */

@Composable
internal fun UrlShortenerScreenApp(modifier: Modifier = Modifier) {
    UrlShortenerApp(modifier = modifier)
}
