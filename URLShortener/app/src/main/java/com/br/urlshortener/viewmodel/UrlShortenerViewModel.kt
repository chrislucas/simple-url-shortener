package com.br.urlshortener.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.br.urlshortener.BuildConfig
import com.br.urlshortener.HttpClient
import com.br.urlshortener.data.remote.UrlShortenerClient
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.domain.repository.RepositoryResult
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

    private val shortUrls = MutableStateFlow<Set<UrlResult>>(emptySet())
    val urls: StateFlow<Set<UrlResult>> = shortUrls.asStateFlow()

    private val mutableUiSate: MutableStateFlow<UrlShortenerUIState> =
        MutableStateFlow(UrlShortenerUIState.Idle)
    val uiState: StateFlow<UrlShortenerUIState> = mutableUiSate.asStateFlow()

    fun putUiOnIdle() = mutableUiSate.update { UrlShortenerUIState.Idle }

    private val mutableUrlShortener = MutableStateFlow<UrlShortener?>(null)
    val urlShortener: StateFlow<UrlShortener?> = mutableUrlShortener.asStateFlow()

    fun onChangeTextFieldContent(newValue: String) {
        mutableTextFieldContent.value = newValue
    }

    fun interpreter(action: UrlShortenerUIEvent) {
        mutableUiSate.update { UrlShortenerUIState.Loading }
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
                UrlShortener.createToPostUrl(url)
            } catch (_: Exception) {
                mutableUiSate.update { UrlShortenerUIState.Error("Invalid URL format") }
                return@launch
            }

            when (val result = repository.postUrl(urlShortener)) {
                is RepositoryResult.Success -> {
                    shortUrls.update { currentShortUrls -> currentShortUrls + result.data }
                    mutableUiSate.update { UrlShortenerUIState.Success(result.data) }
                }

                is RepositoryResult.Error -> {
                    mutableUiSate.update {
                        UrlShortenerUIState.Error(
                            "Failed to post shorten URL." +
                                "\nMessage: ${result.message}.\nStatus Code: ${result.code}."
                        )
                    }
                }
            }
        }
    }

    private fun getUrlShortener(id: String) {
        viewModelScope.launch(coroutineContext) {
            when (val result = repository.getUrlShortener(id)) {
                is RepositoryResult.Success -> {
                    mutableUrlShortener.update {
                        result.data
                    }

                    mutableUiSate.update {
                        UrlShortenerUIState.Success(urlShortener.value)
                    }
                }

                is RepositoryResult.Error -> {
                    mutableUiSate.update {
                        UrlShortenerUIState.Error(
                            "Get Url Shortener Error.\n" +
                                "Message: ${result.message}.\nStatus Code: ${result.code}"
                        )
                    }
                }
            }
        }
    }

    companion object {
        val FACTORY = viewModelFactory {
            initializer {
                val httpClientBuilder = HttpClient.Builder(BuildConfig.BASE_URL)
                val httpClient = httpClientBuilder
                    .withConnectionTimeout(20L)
                    .withReadTimeout(20L)
                    .isDebugMode(BuildConfig.DEBUG)
                    .build()
                val client = httpClient.createService(UrlShortenerClient::class)
                val repository = UrlShortenerRepositoryDefault(client)
                UrlShortenerViewModel(repository)
            }
        }
    }
}
