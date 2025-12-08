package com.payfunds.wallet.modules.settings.security.twofactorauth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.FormsInput
import com.payfunds.wallet.ui.compose.components.HSCircularProgressIndicator
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import io.payfunds.core.helpers.HudHelper

class TwoFactorAuthDisableFragment : BaseComposeFragment() {

    private val viewModel by viewModels<TwoFactorAuthViewModel> {
        TwoFactorAuthModule.Factory()
    }

    @Composable
    override fun GetContent(navController: NavController) {
        TwoFactorAuthDisableScreen(
            close = { navController.popBackStack() },
            viewModel = viewModel,
            navController = navController
        )
    }
}

@Composable
private fun TwoFactorAuthDisableScreen(
    close: () -> Unit,
    viewModel: TwoFactorAuthViewModel,
    navController: NavController
) {
    var otpCode by remember { mutableStateOf("") }
    val disableResponse by viewModel.twoFADisableResponse
    val errorMessage by viewModel.isError
    val isLoading by viewModel.isLoading
    val view = LocalView.current

    disableResponse?.let {
        if (it.isSuccess) {
            DashboardObject.update2FAEnabled(false)
            navController.popBackStack()
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Settings_Google_Authentication),
                backClick = { close() }
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                ButtonPrimaryRed(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                    enabled = true,
                    title = stringResource(R.string.confirm),
                    onClick = {
                        viewModel.twoFADisable(otp = otpCode)
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                VSpacer(12.dp)
                subhead2_grey(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    text = stringResource(R.string.Appearance_2FA_Description),
                    textAlign = TextAlign.Center
                )
                VSpacer(24.dp)

                FormsInput(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    hint = "",
                    pasteEnabled = false,
                    maxLength = 6,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { value ->
                        if (value.length <= 6) {
                            otpCode = value
                        }
                    }
                )
                VSpacer(32.dp)
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                HSCircularProgressIndicator()
            }
        }
        errorMessage?.let {
            LaunchedEffect(it) {
                HudHelper.showErrorMessage(view, it)
            }
        }
    }
}

@Preview
@Composable
private fun TwoFactorAuthDisableScreenPreview() {
    ComposeAppTheme {
        TwoFactorAuthDisableScreen(
            close = {},
            viewModel = viewModel(),
            navController = NavController(LocalContext.current)
        )
    }
}