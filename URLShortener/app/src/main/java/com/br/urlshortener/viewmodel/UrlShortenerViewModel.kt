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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class UrlShortenerViewModel(
    private val repository: UrlShortenerRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : ViewModel() {

    private val mutableTextFieldContent: MutableStateFlow<String> = MutableStateFlow("")
    val textFieldContent: StateFlow<String> = mutableTextFieldContent.asStateFlow()

    private val shortUrls = MutableStateFlow<List<UrlResult>>(emptyList())
    val urls: StateFlow<List<UrlResult>> = shortUrls.asStateFlow()

    private val mutableUiSate: MutableStateFlow<UrlShortenerUIState> = MutableStateFlow(
        UrlShortenerUIState.Idle
    )
    val uiState: StateFlow<UrlShortenerUIState> = mutableUiSate.asStateFlow()

    fun postAction(action: UrlShortenerUIEvent) {
        mutableUiSate.update {
            UrlShortenerUIState.Loading
        }
        when (action) {
            is UrlShortenerUIEvent.PostShortUrlEvent -> {
                val currentUrl = mutableTextFieldContent.value
                postUrl(currentUrl)
            }
            is UrlShortenerUIEvent.GetShortUrlEvent -> {
                getUrlShortener(action.id)
            }
        }
    }

    private fun postUrl(url: String) {
        viewModelScope.launch(coroutineContext) {
            val urlShortener = try {
                UrlShortener.create(url)
            } catch (_: Exception) {
                mutableUiSate.update {
                    UrlShortenerUIState.Error("Invalid URL format")
                }
                return@launch
            }

            try {
                val result = repository.postUrl(urlShortener)
                if (result == null) {
                    mutableUiSate.update {
                        UrlShortenerUIState.Error("Failed to post shorten URL")
                    }
                    return@launch
                }
                shortUrls.update { it + result }
                mutableUiSate.update {
                    UrlShortenerUIState.Success(result)
                }
            } catch (exception: Exception) {
                val message = exception.message ?: "Failed to post shorten URL"
                mutableUiSate.update {
                    UrlShortenerUIState.Error(message)
                }
            }
        }
    }

    private fun getUrlShortener(id: String) {
        viewModelScope.launch(coroutineContext) {
            try {
                val urlResult: UrlResult = repository.getUrlShortener(id)
                mutableUiSate.update {
                    UrlShortenerUIState.Success(urlResult)
                }
            } catch (exception: Exception) {
                val message = exception.message ?: "Failed to fetch shorten URL"
                mutableUiSate.update {
                    UrlShortenerUIState.Error(message)
                }
            }
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
