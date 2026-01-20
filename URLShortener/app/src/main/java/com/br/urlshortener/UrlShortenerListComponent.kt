package com.br.urlshortener

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.theme.URLShortenerTheme
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

@Composable
internal fun UrlShortenerListComponent(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel
) {
    val urls by urlShortenerViewModel.urls.collectAsState()
    UrlShortenerList(modifier, urls) { id ->
        urlShortenerViewModel.postAction(
            UrlShortenerUIEvent.GetShortUrlEvent(id)
        )
    }
}

@Composable
internal fun UrlShortenerList(
    modifier: Modifier = Modifier,
    urls: List<UrlResult>,
    onClickListener: (String) -> Unit
) {
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
                    onClickListener(url.alias)
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

class UrlShortenerListParameterProvider : PreviewParameterProvider<List<UrlResult>> {
    override val values: Sequence<List<UrlResult>> = sequenceOf(
        listOf(
            UrlResult(
                alias = "1",
                link = Link(
                    self = "https://www.google.com",
                    short = "https://url-shortener/1"
                )
            ),
            UrlResult(
                alias = "2",
                link = Link(
                    self = "https://www.facebook.com",
                    short = "https://url-shortener/2"
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun UrlShortenerListPreview(
    @PreviewParameter(UrlShortenerListParameterProvider::class) urls: List<UrlResult>
) {
    URLShortenerTheme {
        UrlShortenerList(
            urls = urls,
            onClickListener = {}
        )
    }
}
