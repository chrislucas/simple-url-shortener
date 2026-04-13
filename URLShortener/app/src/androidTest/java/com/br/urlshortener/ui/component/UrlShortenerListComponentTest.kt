package com.br.urlshortener.ui.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.br.urlshortener.domain.model.Link
import com.br.urlshortener.domain.model.UrlResult
import com.br.urlshortener.ui.event.UrlShortenerUIEvent
import com.br.urlshortener.viewmodel.UrlShortenerViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class UrlShortenerListComponentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun given_a_list_url_result_when_create_url_shortener_list_then_should_display_the_correct_quantity() {
        val urls = listOf(
            UrlResult(
                alias = "alias-1",
                link = Link(self = "https://example.com/1", short = "https://short/1")
            ),
            UrlResult(
                alias = "alias-2",
                link = Link(self = "https://example.com/2", short = "https://short/2")
            )
        )

        composeRule.setContent {
            UrlShortenerList(
                urls = urls,
                onClickListener = {}
            )
        }

        composeRule.onNodeWithText("Shorted URL: https://short/1", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Shorted URL: https://short/2", substring = true).assertIsDisplayed()
    }

    @Test
    fun given_a_list_url_result_when_click_on_item_then_should_invoke_on_click_listener() {
        val urls = listOf(
            UrlResult(
                alias = "alias-1",
                link = Link(self = "https://example.com/1", short = "https://short/1")
            )
        )
        val onClickListener = mockk<(String) -> Unit>(relaxed = true)

        composeRule.setContent {
            UrlShortenerList(
                urls = urls,
                onClickListener = onClickListener
            )
        }

        composeRule.onNodeWithText("Shorted URL: https://short/1", substring = true).performClick()

        verify(exactly = 1) {
            onClickListener("alias-1")
        }
    }

    @Test
    fun given_an_empty_list_when_create_url_shortener_list_then_no_item_should_have_click_action() {
        composeRule.setContent {
            UrlShortenerList(
                urls = emptyList(),
                onClickListener = {}
            )
        }

        composeRule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

}
