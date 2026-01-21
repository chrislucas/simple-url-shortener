package com.br.urlshortener.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorOverlayComponentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun given_custom_message_when_render_error_overlay_then_show_message() {
        composeRule.setContent {
            ErrorOverlayComponent(message = "Network error")
        }

        composeRule.onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun given_no_message_when_render_error_overlay_then_show_default_message() {
        composeRule.setContent {
            ErrorOverlayComponent()
        }

        composeRule.onNodeWithText("Android").assertIsDisplayed()
    }
}
