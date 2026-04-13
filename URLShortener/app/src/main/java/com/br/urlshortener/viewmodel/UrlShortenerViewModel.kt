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
import com.br.urlshortener.ui.event.UrlShortenerEvent
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.ui.state.UrlShortenerUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlin.coroutines.CoroutineContext

class UrlShortenerViewModel(
    private val repository: UrlShortenerRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO,
    private val delayMillisOnError: Long = 2000L
) : ViewModel() {

    private val mutableTextFieldContent: MutableStateFlow<String> = MutableStateFlow("")
    val textFieldContent: StateFlow<String> = mutableTextFieldContent.asStateFlow()

    private val shortUrls = MutableStateFlow<Set<UrlResult>>(emptySet())
    val urls: StateFlow<Set<UrlResult>> = shortUrls.asStateFlow()

    private val mutableUiState: MutableStateFlow<UrlShortenerUIState> = MutableStateFlow(
        UrlShortenerUIState.Idle
    )
    val uiState: StateFlow<UrlShortenerUIState> = mutableUiState.asStateFlow()

    private val mutableUrlShortener = MutableStateFlow<UrlShortener?>(null)
    val urlShortener: StateFlow<UrlShortener?> = mutableUrlShortener.asStateFlow()

    private val mutableNavigationEvent = MutableSharedFlow<UrlShortenerEvent>(replay = 0, extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<UrlShortenerEvent> = mutableNavigationEvent.asSharedFlow()

    fun putUiOnIdle() {
        mutableUiState.update { UrlShortenerUIState.Idle }
        clearUrlShortener()
    }

    private fun clearUrlShortener() {
        mutableUrlShortener.update { null }
    }

    fun onChangeTextFieldContent(newValue: String) {
        mutableTextFieldContent.update { newValue }
    }

    fun uiEventInterpreter(uiEvent: UrlShortenerUIEvent) {
        when (uiEvent) {
            is UrlShortenerUIEvent.PostShortUrlEvent -> {
                mutableUiState.update { UrlShortenerUIState.Loading }
                val currentUrl = mutableTextFieldContent.value
                postUrl(currentUrl)
            }

            is UrlShortenerUIEvent.GetShortUrlEvent -> {
                clearUrlShortener()
                getUrlShortener(uiEvent.id)
            }
        }
    }

    private fun postUrl(url: String) {
        viewModelScope.launch(coroutineContext) {
            val urlShortener = try {
                UrlShortener.createToPostUrl(url)
            } catch (_: Exception) {
                val message = "Invalid URL format: $url"
                mutableUiState.update { UrlShortenerUIState.Error(message) }
                delay(delayMillisOnError)

                mutableUiState.update { UrlShortenerUIState.Idle }
                mutableNavigationEvent.emit(UrlShortenerEvent.ShowError(message))
                return@launch
            }

            when (val result = repository.postUrl(urlShortener)) {
                is RepositoryResult.Success -> {
                    shortUrls.update { currentShortUrls -> currentShortUrls + result.data }
                    mutableUiState.update { UrlShortenerUIState.Idle }
                    mutableNavigationEvent.emit(UrlShortenerEvent.ShowSnackBar("URL encurtada com sucesso"))
                }

                is RepositoryResult.Error -> {
                    val message =
                        "Failed to post shorten URL.\nMessage: ${result.message}. Status Code: ${result.code}."
                    mutableUiState.update {
                        UrlShortenerUIState.Error(message)
                    }

                    delay(delayMillisOnError)
                    mutableUiState.update { UrlShortenerUIState.Idle }
                    mutableNavigationEvent.emit(UrlShortenerEvent.ShowError(message))
                }
            }
        }
    }

    private fun getUrlShortener(id: String) {
        viewModelScope.launch(coroutineContext) {
            when (val result = repository.getUrlShortener(id)) {
                is RepositoryResult.Success -> {
                    mutableUrlShortener.update { result.data }
                    mutableUiState.update { UrlShortenerUIState.Idle }
                    mutableNavigationEvent.emit(UrlShortenerEvent.NavigateToDetail)
                }

                is RepositoryResult.Error -> {
                    mutableUiState.update { UrlShortenerUIState.Idle }
                    mutableNavigationEvent.emit(
                        UrlShortenerEvent.ShowError(
                            "Get Url Shortener Error.\nMessage: ${result.message}. Status Code: ${result.code}"
                        )
                    )
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
