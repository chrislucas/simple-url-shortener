package com.br.urlshortener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
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

enum class UrlShortenerScreen(@field:StringRes val title: Int) {
    Start(title = R.string.app_name),
    ListShortenerUrl(title = R.string.list_shortener_url),
    ShortenerUrDetail(title = R.string.shortener_url_detail),
}

@Preview
@Composable
internal fun UrlShortenerApp() {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlShortenerAppBar(
    currentScreen: UrlShortenerScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Preview("UrlShortenerScreenPreview", showSystemUi = true)
@Composable
internal fun UrlShortenerScreen(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel = viewModel(factory = UrlShortenerViewModel.FACTORY)
) {

    val uiState = urlShortenerViewModel.uiState.collectAsState()

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
            modifier = Modifier.weight(.9f),
            value = content,
            onValueChange = urlShortenerViewModel::onChangeTextFieldContent,
            label = { Text(text = stringResource(id = R.string.enter_valid_url)) },
        )

        Button(
            modifier = Modifier.weight(.1f),
            onClick = {
                urlShortenerViewModel.postAction(UrlShortenerUIEvent.PostShortUrlEvent)
            }
        ) {
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
            Card(
                modifier = modifier
                    .fillParentMaxWidth()
                    .padding(8.dp),
                onClick = {
                    urlShortenerViewModel.postAction(
                        UrlShortenerUIEvent.GetShortUrlEvent(url.alias)
                    )
                }
            ) {
                Text(
                    text = url.link.short,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}