package com.payfunds.wallet.ui.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import io.payfunds.core.helpers.HudHelper

@Composable
fun SnackbarError(errorMessage: String) {
    HudHelper.showErrorMessage(LocalView.current, errorMessage)
}
