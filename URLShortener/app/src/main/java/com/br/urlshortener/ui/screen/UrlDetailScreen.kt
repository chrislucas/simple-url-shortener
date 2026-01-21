package com.br.urlshortener.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.br.urlshortener.ui.theme.URLShortenerTheme

@Composable
fun UrlDetailScreen(
    url: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .fillMaxSize()
        .systemBarsPadding()
        .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "URL $url!",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UrlDetailScreenPreview() {
    URLShortenerTheme {
        UrlDetailScreen("https://example.com")
    }
}
