package com.payfunds.wallet.modules.settings.security.twofactorauth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.network.response_model.two_factor_auth.qr.RecoveryCode
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.HsIconButton
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.headline1_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.compose.components.subhead2_red50

class TwoFactorAuthRecoverCodeFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        TwoFactorAuthRecoverCodeScreen(
            navController = navController
        )
    }
}

@Composable
fun TwoFactorAuthRecoverCodeScreen(
    navController: NavController
) {
    val recoveryCode = navController.getInput<RecoveryCode>()
    if (recoveryCode == null) {
        navController.popBackStack()
        return
    }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var walletAddress by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val account = App.accountManager.activeAccount
        account?.let {
            val address = getWalletAddress(account)
            walletAddress = address ?: ""
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Settings_Google_Authentication),
                backClick = {
                    DashboardObject.updateIsDashboardOpened(true)
                    navController.popBackStack(R.id.securitySettingsFragment, false)
                }
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                ButtonPrimaryRed(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                    enabled = isCopied,
                    title = stringResource(R.string.proceed),
                    onClick = {
                        DashboardObject.updateIsDashboardOpened(true)
                        navController.popBackStack(R.id.securitySettingsFragment, false)
                    })
            }
        }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                VSpacer(12.dp)
                headline1_leah(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.two_factor_auth_save_your_recovery_code)
                )

                VSpacer(24.dp)

                subhead2_grey(text = stringResource(R.string.two_factor_recovery_code_description))

                VSpacer(32.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    headline1_leah(text = recoveryCode.recoveryCode)

                    HSpacer(4.dp)

                    HsIconButton(modifier = Modifier.size(24.dp), onClick = {
                        clipboardManager.setText(AnnotatedString(recoveryCode.recoveryCode))
                        isCopied = true
                    }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_copy_20),
                            contentDescription = null,
                            tint = ComposeAppTheme.colors.leah
                        )
                    }
                }
                VSpacer(32.dp)
                subhead2_red50(text = stringResource(R.string.two_factor_auth_note))
                VSpacer(32.dp)
            }
        }
    }
}

@Preview
@Composable
fun TwoFactorAuthRecoverCopyScreenPreview() {
    ComposeAppTheme {
        TwoFactorAuthRecoverCodeScreen(
            navController = NavController(LocalContext.current)
        )
    }
}