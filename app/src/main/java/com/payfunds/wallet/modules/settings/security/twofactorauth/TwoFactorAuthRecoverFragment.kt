package com.payfunds.wallet.modules.settings.security.twofactorauth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.FormsInput
import com.payfunds.wallet.ui.compose.components.HSCircularProgressIndicator
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.headline1_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.compose.components.subhead2_grey50
import io.payfunds.core.helpers.HudHelper

class TwoFactorAuthRecoverFragment : BaseComposeFragment() {

    private val viewModel by viewModels<TwoFactorAuthViewModel> {
        TwoFactorAuthModule.Factory()
    }

    @Composable
    override fun GetContent(navController: NavController) {
        TwoFactorAuthRecoverScreen(
            close = { navController.popBackStack() },
            navController = navController,
            viewModel = viewModel,
        )
    }
}

@Composable
private fun TwoFactorAuthRecoverScreen(
    close: () -> Unit,
    navController: NavController,
    viewModel: TwoFactorAuthViewModel
) {
    val view = LocalView.current
    val recoveryResponse by viewModel.twoFARecoverResponse
    val errorMessage by viewModel.isError
    val isLoading by viewModel.isLoading

    var recoveryCode by remember { mutableStateOf("") }
    var walletAddress by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val account = App.accountManager.activeAccount
        account?.let {
            val address = getWalletAddress(account)
            walletAddress = address ?: ""
        }
    }

    recoveryResponse?.let {
        if (it.isSuccess) {
            DashboardObject.updateIsDashboardOpened(true)
            navController.popBackStack(R.id.mainFragment, false)
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Settings_Google_Authentication),
                backClick = { close() }
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                ButtonPrimaryRed(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = recoveryCode.isNotEmpty(),
                    title = stringResource(R.string.verify),
                    onClick = {
                        viewModel.twoFARecovery(
                            walletAddress = walletAddress,
                            recoveryCode = recoveryCode
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                VSpacer(12.dp)
                Icon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(48.dp),
                    painter = painterResource(id = R.drawable.ic_recovery),
                    contentDescription = "Google Authenticator",
                    tint = ComposeAppTheme.colors.jacob
                )

                VSpacer(16.dp)

                headline1_leah(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.Google_Authenticator_User_Recovery_Code)
                )

                VSpacer(24.dp)

                subhead2_grey(text = stringResource(R.string.google_auth_user_recovery_code_description))

                VSpacer(24.dp)

                subhead2_grey(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = stringResource(R.string.Google_Authenticator_Recovery_Code)
                )

                FormsInput(
                    hint = "",
                    pasteEnabled = false,
                    maxLength = 16,
                    onValueChange = { value ->
                        if (value.length <= 16) {
                            recoveryCode = value
                        }
                    }
                )
                VSpacer(16.dp)
                subhead2_grey50(text = stringResource(R.string.google_auth_user_note))
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
private fun TwoFactorAuthRecoverPreview() {
    ComposeAppTheme {
        TwoFactorAuthRecoverScreen(
            close = {},
            viewModel = viewModel(),
            navController = NavController(LocalContext.current)
        )
    }
}