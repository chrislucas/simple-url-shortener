package com.br.urlshortener.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.br.urlshortener.BuildConfig
import com.br.urlshortener.HttpClientBuilder
import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.UrlShortenerRepository
import com.br.urlshortener.domain.repository.UrlShortenerRepositoryDefault
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UrlShortenerViewModel(
    private val repository: UrlShortenerRepository
) : ViewModel() {

    private val mutableTextFieldContent: MutableStateFlow<String> = MutableStateFlow("")
    val textFieldContent: StateFlow<String> = mutableTextFieldContent.asStateFlow()

    private val shortUrls = MutableStateFlow(mutableListOf<UrlResult>())
    val urls: StateFlow<List<UrlResult>> = shortUrls.asStateFlow()

    private val mutableUiSate: MutableStateFlow<UrlShortenerUIState> = MutableStateFlow(
        UrlShortenerUIState.Idle
    )
    val uiState: StateFlow<UrlShortenerUIState> = mutableUiSate.asStateFlow()

    fun postAction(action: UrlShortenerUIEvent) {
        when (action) {
            is UrlShortenerUIEvent.PostShortUrlEvent -> {
                postUrl()
            }

            is UrlShortenerUIEvent.GetShortUrlEvent -> {
                getUrlShortener(action.id)
            }
        }
    }

    private fun postUrl() {
        viewModelScope.launch {
            val url = mutableTextFieldContent.value
            runCatching {
                mutableUiSate.value = UrlShortenerUIState.Loading
                UrlShortener.create(url)
            }.onSuccess { urlShortener ->
                repository.postUrl(urlShortener)?.let { result ->
                    val currentList = shortUrls.value
                    currentList.add(0, result)
                    shortUrls.value = currentList
                    mutableUiSate.value = UrlShortenerUIState.Success(result)
                } ?: run {
                    mutableUiSate.value = UrlShortenerUIState.Error("Failed to post shorten URL")
                }
            }.onFailure {
                // Handle failure if needed
                mutableUiSate.value = UrlShortenerUIState.Error("Invalid URL format")
            }
        }
    }

    private fun getUrlShortener(id: String) {
        viewModelScope.launch {
            val urlResult: UrlResult = repository.getUrlShortener(id)
        }
    }

    fun onChangeTextFieldContent(newValue: String) {
        mutableTextFieldContent.value = newValue
    }

    companion object {
        val FACTORY = viewModelFactory {
            initializer {
                val client = HttpClientBuilder.createService<UrlShortenerClient>(BuildConfig.BASE_URL)
                val repository = UrlShortenerRepositoryDefault(client)
                UrlShortenerViewModel(repository)
            }
        }
    }
}
