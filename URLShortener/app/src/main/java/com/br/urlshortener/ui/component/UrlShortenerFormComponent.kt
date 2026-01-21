package com.br.urlshortener.ui.component

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.br.urlshortener.R
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.theme.URLShortenerTheme
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

@Composable
internal fun UrlShortenerFormComponent(
    modifier: Modifier = Modifier,
    urlShortenerViewModel: UrlShortenerViewModel
) {
    val content by urlShortenerViewModel.textFieldContent.collectAsState()
    UrlShortenerForm(
        content = content,
        onContentChange = urlShortenerViewModel::onChangeTextFieldContent,
        onSendClick = {
            urlShortenerViewModel.interpreter(UrlShortenerUIEvent.PostShortUrlEvent)
        },
        modifier = modifier
    )
}

@Composable
private fun UrlShortenerForm(
    content: String,
    onContentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(.6f)
                .fillMaxHeight(),
            value = content,
            onValueChange = onContentChange,
            label = { Text(text = stringResource(id = R.string.enter_valid_url)) },
        )

        Button(
            modifier = Modifier
                .weight(.4f)
                .fillMaxHeight()
                .padding(4.dp),
            onClick = onSendClick,
            shape = RectangleShape
        ) {
            Text(text = stringResource(id = R.string.send_shorten_url))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UrlShortenerFormComponentPreview() {
    URLShortenerTheme {
        UrlShortenerForm(
            content = "https://www.google.com",
            onContentChange = {},
            onSendClick = {},
            modifier = Modifier
                .systemBarsPadding()
                .navigationBarsPadding()
        )
    }
}
