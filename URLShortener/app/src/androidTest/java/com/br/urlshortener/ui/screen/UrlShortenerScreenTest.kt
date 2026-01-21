package com.br.urlshortener.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.domain.model.UrlShortener
import com.br.urlshortener.ui.state.UrlShortenerUIState
import com.br.urlshortener.viewmodel.UrlShortenerViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UrlShortenerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun given_loading_state_when_render_screen_then_show_loading_and_form() {
        val viewModel = mockViewModel(uiState = UrlShortenerUIState.Loading)

        composeRule.setContent {
            UrlShortenerScreen(urlShortenerViewModel = viewModel)
        }

        composeRule.onNodeWithText("Enter With a valid URL").assertIsDisplayed()
        composeRule.onNodeWithText("enviar").assertIsDisplayed()
    }

    @Test
    fun given_error_state_when_render_screen_then_show_error_and_form() {
        val viewModel = mockViewModel(uiState = UrlShortenerUIState.Error("Any error"))

        composeRule.setContent {
            UrlShortenerScreen(urlShortenerViewModel = viewModel)
        }

        composeRule.onNodeWithText("Any error").assertIsDisplayed()
        composeRule.onNodeWithText("Enter With a valid URL").assertIsDisplayed()
        composeRule.onNodeWithText("enviar").assertIsDisplayed()
    }

    @Test
    fun given_success_with_url_shortener_when_url_not_empty_then_show_detail_and_back() {
        val url = "https://short.url/abc"
        val viewModel = mockViewModel(
            uiState = UrlShortenerUIState.Success(UrlShortener.createFromGetResult(url))
        )
        val onBackPressed = mockk<() -> Unit>(relaxed = true)

        composeRule.setContent {
            UrlShortenerScreen(
                urlShortenerViewModel = viewModel,
                onBackPressed = onBackPressed
            )
        }

        composeRule.onNodeWithText("URL $url!").assertIsDisplayed()
        composeRule.onNodeWithText("voltar").performClick()

        verify(exactly = 1) { onBackPressed() }
    }

    @Test
    fun given_success_with_url_shortener_when_url_empty_then_show_form() {
        val viewModel = mockViewModel(
            uiState = UrlShortenerUIState.Success(UrlShortener.createFromGetResult(""))
        )

        composeRule.setContent {
            UrlShortenerScreen(urlShortenerViewModel = viewModel)
        }

        composeRule.onNodeWithText("Enter With a valid URL").assertIsDisplayed()
        composeRule.onNodeWithText("enviar").assertIsDisplayed()
    }

    @Test
    fun given_success_with_other_type_when_render_screen_then_show_form() {
        val result = UrlResult(
            alias = "alias-1",
            link = Link(self = "https://example.com/1", short = "https://short/1")
        )
        val viewModel = mockViewModel(uiState = UrlShortenerUIState.Success(result))

        composeRule.setContent {
            UrlShortenerScreen(urlShortenerViewModel = viewModel)
        }

        composeRule.onNodeWithText("Enter With a valid URL").assertIsDisplayed()
        composeRule.onNodeWithText("enviar").assertIsDisplayed()
    }

    @Test
    fun given_idle_state_when_render_screen_then_show_form() {
        val viewModel = mockViewModel(uiState = UrlShortenerUIState.Idle)

        composeRule.setContent {
            UrlShortenerScreen(urlShortenerViewModel = viewModel)
        }

        composeRule.onNodeWithText("Enter With a valid URL").assertIsDisplayed()
        composeRule.onNodeWithText("enviar").assertIsDisplayed()
    }

    private fun mockViewModel(
        uiState: UrlShortenerUIState,
        textFieldContent: String = "",
        urls: List<UrlResult> = emptyList()
    ): UrlShortenerViewModel {
        val viewModel = mockk<UrlShortenerViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(uiState)
        every { viewModel.textFieldContent } returns MutableStateFlow(textFieldContent)
        every { viewModel.urls } returns MutableStateFlow(urls)
        return viewModel
    }
}
