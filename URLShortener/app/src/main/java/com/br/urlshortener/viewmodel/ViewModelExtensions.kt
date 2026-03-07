package com.br.urlshortener.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T : ViewModel> ComponentActivity.viewModelBuilder(): Lazy<T> = lazy {
    ViewModelProvider(this)[T::class.java]
}

// private val viewModel by viewModelBuilder<UrlShortenerViewModel>()
