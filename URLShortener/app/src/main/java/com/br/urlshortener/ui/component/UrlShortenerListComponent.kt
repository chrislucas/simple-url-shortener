package com.br.urlshortener.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.ui.theme.URLShortenerTheme

@Composable
internal fun UrlShortenerListComponent(
    modifier: Modifier = Modifier,
    urls: List<UrlResult>,
    onClickItem: (String) -> Unit
) {
    UrlShortenerList(modifier, urls, onClickItem)
}

@Composable
internal fun UrlShortenerList(
    modifier: Modifier = Modifier,
    urls: List<UrlResult>,
    onClickListener: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = urls, key = { url -> url.alias }) { url ->
            Card(
                modifier = modifier
                    .fillParentMaxWidth()
                    .padding(4.dp),
                onClick = { onClickListener(url.alias) },
                shape = RectangleShape
            ) {
                Column {
                    Text(
                        text = "Shorted URL: ${url.link.short}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Original URL: ${url.link.self}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
