package com.br.urlshortener.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import com.br.urlshortener.ui.theme.URLShortenerTheme
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

@Composable
internal fun UrlShortenerListComponent(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel,
    onClickItem: () -> Unit = {}
) {
    val uiState by urlShortenerViewModel.uiState.collectAsState()
    when (uiState) {
        is UrlShortenerUIState.Success<*> -> {
            val urlShortener = (uiState as UrlShortenerUIState.Success<*>).data
            if (urlShortener is UrlShortener) {
                onClickItem()
            }
        }

        is UrlShortenerUIState.Error -> {
            //
        }

        else -> {
            // DO NOTHINH
        }
    }
    val urls by urlShortenerViewModel.urls.collectAsState()
    UrlShortenerList(modifier, urls) { id ->
        urlShortenerViewModel.interpreter(UrlShortenerUIEvent.GetShortUrlEvent(id))
    }
}

@Composable
internal fun UrlShortenerList(
    modifier: Modifier = Modifier,
    urls: List<UrlResult>,
    onClickListener: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(urls.size) { index ->
            val url = urls[index]
            Card(
                modifier = modifier
                    .fillParentMaxWidth()
                    .padding(4.dp),
                onClick = { onClickListener(url.alias) },
                shape = RectangleShape
            ) {
                Text(
                    text = url.link.short,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                )
            }
        }
    }
}

class UrlShortenerListParameterProvider : PreviewParameterProvider<List<UrlResult>> {
    override val values: Sequence<List<UrlResult>> = sequenceOf(
        buildList {
            repeat(10) {
                add(
                    UrlResult(
                        alias = "$it",
                        link = Link(
                            self = "https://www.google.com",
                            short = "https://url-shortener/1"
                        )
                    )
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun UrlShortenerListPreview(
    @PreviewParameter(UrlShortenerListParameterProvider::class) urls: List<UrlResult>
) {
    URLShortenerTheme {
        UrlShortenerList(
            urls = urls,
            onClickListener = {}
        )
    }
}
