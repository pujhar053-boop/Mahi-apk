package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.ui.MahiViewModel

@Composable
fun MahiCompanionScreen(
    viewModel: MahiViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.onAppStarted()
    }

    MahiHomeScreen(viewModel = viewModel, modifier = modifier)
}
