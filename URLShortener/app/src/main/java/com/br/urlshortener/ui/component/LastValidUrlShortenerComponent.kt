package com.br.urlshortener.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.ui.theme.URLShortenerTheme

/*
    Criar um componente que mostre a última URL validada enviada para o backend com
    um botão no final da que leva para a lista de todas as urls já enviadas
 */

@Composable
fun LastValidShortUrl(
    modifier: Modifier = Modifier,
    url: UrlResult,
    onClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column {
            Text(
                text = "Shorted URL: ${url.link.short}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp),
            )
            Text(
                text = "Original URL: ${url.link.self}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp),
            )
        }

        Button(
            onClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(2.dp),
            shape = RectangleShape
        ) {
            Text("List URLS")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LastValidShortUrlPreview() {
    URLShortenerTheme {
        LastValidShortUrl(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .statusBarsPadding(),
            url = UrlResult(
                alias = "",
                link = Link(
                    short = "",
                    self = ""
                )
            )
        )
    }
}
