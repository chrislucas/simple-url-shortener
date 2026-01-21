package com.br.urlshortener.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.br.urlshortener.ui.theme.URLShortenerTheme

@Composable
fun OriginalUrlComponent(url: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = "URL $url!",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OriginalUrlComponentPreview() {
    URLShortenerTheme {
        OriginalUrlComponent("https://example.com")
    }
}
