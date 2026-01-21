package com.br.urlshortener.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadingOverlayComponentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun given_loading_overlay_when_render_then_show_progress_indicator() {
        composeRule.setContent {
            LoadingOverlayComponent()
        }

        composeRule.onNodeWithTag("progress_indicator").assertIsDisplayed()
    }

    @Test
    fun given_loading_overlay_when_render_then_fill_available_space() {
        composeRule.setContent {
            Box(modifier = Modifier.size(200.dp).testTag("container")) {
                LoadingOverlayComponent()
            }
        }

        val overlayBounds = composeRule.onNodeWithTag("loading_overlay").getUnclippedBoundsInRoot()
        val containerBounds = composeRule.onNodeWithTag("container").getUnclippedBoundsInRoot()

        assertEquals(containerBounds.width, overlayBounds.width)
        assertEquals(containerBounds.height, overlayBounds.height)
    }
}
