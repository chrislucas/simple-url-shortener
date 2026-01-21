package com.br.urlshortener.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun given_splash_screen_when_render_then_show_logo_and_app_name() {
        composeRule.setContent {
            SplashScreen()
        }

        composeRule.onNodeWithContentDescription("Logo").assertIsDisplayed()
        composeRule.onNodeWithText("URLShortener").assertIsDisplayed()
    }

    @Test
    fun given_splash_screen_when_time_passes_then_should_call_on_close_once() {
        val onClose = mockk<() -> Unit>(relaxed = true)

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            SplashScreen(onClose = onClose)
        }

        composeRule.mainClock.advanceTimeBy(2499)
        verify(exactly = 0) { onClose() }

        composeRule.mainClock.advanceTimeBy(1)
        verify(exactly = 1) { onClose() }
    }
}
