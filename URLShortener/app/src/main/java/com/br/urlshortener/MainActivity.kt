package com.br.urlshortener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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


@Preview("UrlShortenerScreenPreview", showSystemUi = true)
@Composable
internal fun UrlShortenerScreen(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(factory = UrlShortenerViewModel.FACTORY)
) {

    UrlShortenerFormComponent(modifier, urlShortenerViewModel)
    UrlShortenerListComponent(modifier, urlShortenerViewModel)
}

@Composable
private fun UrlShortenerFormComponent(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(
        factory = UrlShortenerViewModel.FACTORY
    )
) {

    val content by urlShortenerViewModel.textFieldContent.collectAsState()
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = {},
            label = { Text(text = stringResource(id = R.string.enter_valid_url)) },
        )

        Button(onClick = {}) {
            Text(text = stringResource(id = R.string.send_shorten_url))
        }
    }
}

@Composable
private fun UrlShortenerListComponent(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(factory = UrlShortenerViewModel.FACTORY)
) {
    val urls by urlShortenerViewModel.urls.collectAsState()
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(urls.size) { index ->
            val url = urls[index]
            Text(
                text = url.link.short,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}