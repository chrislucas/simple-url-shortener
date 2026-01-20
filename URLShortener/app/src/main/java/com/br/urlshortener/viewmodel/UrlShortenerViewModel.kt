package com.br.urlshortener.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.Url
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.UrlShortenerRepository
import com.br.urlshortener.domain.repository.UrlShortenerRepositoryDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UrlShortenerViewModel(
    private val repository: UrlShortenerRepository
) : ViewModel() {

    private val mutableTextFieldContent: MutableStateFlow<String> = MutableStateFlow("")

    val textFieldContent: StateFlow<String> = mutableTextFieldContent.asStateFlow()



    private val shortUrls = MutableStateFlow(
        listOf(
            Url(
                alias = "79673226",
                link = Link(
                    self = "https://api.short.io/links/79673226",
                    short = "https://short.io/79673226"
                )
            ),
            Url(
                alias = "example1",
                link = Link(
                    self = "https://api.short.io/links/example1",
                    short = "https://short.io/example1"
                )
            ),
            Url(
                alias = "example2",
                link = Link(
                    self = "https://api.short.io/links/example2",
                    short = "https://short.io/example2"
                )

            )
        )
    )

    val urls: StateFlow<List<Url>> = shortUrls.asStateFlow()

    suspend fun postUrl(url: String) {
        viewModelScope.launch {
            repository.postUrl(url)
        }
    }

    suspend fun getUrlShortener(id: String) {
        viewModelScope.launch {


        }
    }

    companion object {
        /*
        val FACTORY: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = UrlShortenerRepositoryDefault() // Replace with actual implementation
                return UrlShortenerViewModel(repository) as T
            }
        }

         */

        val FACTORY = viewModelFactory {
            initializer {
                val repository = UrlShortenerRepositoryDefault() // Replace with actual implementation
                UrlShortenerViewModel(repository)
            }
        }
    }
}