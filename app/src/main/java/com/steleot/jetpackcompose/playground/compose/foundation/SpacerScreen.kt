package com.steleot.jetpackcompose.playground.compose.foundation

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steleot.jetpackcompose.playground.compose.reusable.DefaultScaffold
import com.steleot.jetpackcompose.playground.navigation.FoundationNavRoutes

private const val Url = "foundation/SpacerScreen.kt"

@Composable
fun SpacerScreen() {
    DefaultScaffold(
        title = FoundationNavRoutes.Spacer,
        link = Url,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = "There"
            )
            Spacer(modifier = Modifier.requiredHeight(16.dp))
            Text(
                text = "is"
            )
            Spacer(modifier = Modifier.requiredHeight(32.dp))
            Text(
                text = "spacer"
            )
            Spacer(modifier = Modifier.requiredHeight(16.dp))
            Row {
                Text(
                    text = "between"
                )
                Spacer(modifier = Modifier.requiredWidth(16.dp))
                Text(
                    text = "even"
                )
                Spacer(modifier = Modifier.requiredWidth(16.dp))
                Text(
                    text = "in"
                )
                Spacer(modifier = Modifier.requiredWidth(32.dp))
                Text(
                    text = "row"
                )
                Spacer(modifier = Modifier.requiredWidth(16.dp))
            }
        }
    }
}