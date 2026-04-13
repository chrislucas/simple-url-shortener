package com.br.urlshortener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.br.urlshortener.ui.screen.UrlShortenerApp
import com.br.urlshortener.ui.theme.URLShortenerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            URLShortenerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UrlShortenerScreenApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
internal fun UrlShortenerScreenApp(modifier: Modifier = Modifier) {
    UrlShortenerApp(modifier = modifier)
}
