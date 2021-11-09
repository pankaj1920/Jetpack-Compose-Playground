package com.steleot.jetpackcompose.playground.compose.materialicons

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.steleot.jetpackcompose.playground.MainActivity
import com.steleot.jetpackcompose.playground.compose.theme.TestTheme
import org.junit.Rule
import org.junit.Test

class FilledScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFilledScreen() {
        composeTestRule.setContent {
            TestTheme {
                FilledScreen()
            }
        }
        // todo
    }
}