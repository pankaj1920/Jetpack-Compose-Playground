package com.steleot.jetpackcompose.playground.compose.externallibraries

import androidx.compose.ui.test.junit4.createComposeRule
import com.steleot.jetpackcompose.playground.compose.theme.TestTheme
import org.junit.Rule
import org.junit.Test

class SSJetPackComposeProgressButtonScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSSJetPackComposeProgressButtonScreen() {
        composeTestRule.setContent {
            TestTheme {
                SSJetPackComposeProgressButtonScreen()
            }
        }
        // todo
    }
}