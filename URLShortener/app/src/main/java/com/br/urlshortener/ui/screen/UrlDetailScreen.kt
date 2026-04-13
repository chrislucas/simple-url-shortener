package com.br.urlshortener.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.br.urlshortener.R
import com.br.urlshortener.ui.theme.URLShortenerTheme

@Composable
fun UrlDetailScreen(
    url: String,
    onBackPressed: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .systemBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "URL $url!",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = TextUnit(22f, TextUnitType.Sp),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.SansSerif
            )
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onBackPressed() },
            shape = RectangleShape
        ) {
            Text(stringResource(R.string.back_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UrlDetailScreenPreview() {
    URLShortenerTheme(dynamicColor = false) {
        UrlDetailScreen("https://example.com")
    }
}
