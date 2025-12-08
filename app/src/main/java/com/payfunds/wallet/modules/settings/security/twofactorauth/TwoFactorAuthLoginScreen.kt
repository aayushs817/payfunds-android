package com.payfunds.wallet.modules.settings.security.twofactorauth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.FormsInput
import com.payfunds.wallet.ui.compose.components.HSCircularProgressIndicator
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.headline1_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.compose.components.subhead2_leah
import io.payfunds.core.helpers.HudHelper

@Composable
fun TwoFactorAuthLoginScreen(
    navController: NavController,
    onSuccessfulAuth: (Boolean) -> Unit,
) {
    val twoFactorAuthViewModel =
        viewModel<TwoFactorAuthViewModel>(factory = TwoFactorAuthModule.Factory())
    var otpCode by remember { mutableStateOf("") }
    val verifyResponse by twoFactorAuthViewModel.twoFAVerifyResponse
    val errorMessage by twoFactorAuthViewModel.isError
    val isLoading by twoFactorAuthViewModel.isLoading
    val view = LocalView.current


//    LaunchedEffect(Unit) {
//        val account = App.accountManager.activeAccount
//        account?.let {
//            val address = getWalletAddress(it)
//            walletAddress = address ?: ""
//        }
//    }

    LaunchedEffect(twoFactorAuthViewModel.twoFAVerifyResponse.value) {
        twoFactorAuthViewModel.twoFAVerifyResponse.value?.let {
            onSuccessfulAuth(false)
        }
        twoFactorAuthViewModel.twoFAVerifyResponse.value = null
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposeAppTheme.colors.tyler)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                VSpacer(24.dp)
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
                    text = stringResource(R.string.two_factor_please_enter_code)
                )
                VSpacer(24.dp)

                subhead2_grey(text = stringResource(R.string.two_factor_description))

                VSpacer(24.dp)

                FormsInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
            }

            Column(
                modifier = Modifier
                    .weight(0.5f),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VSpacer(12.dp)
                ButtonPrimaryRed(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    title = stringResource(R.string.verify),
                    enabled = otpCode.isNotEmpty(),
                    onClick = {
                        val account = App.accountManager.activeAccount
                        account?.let {
                            val address = getWalletAddress(it)
                            twoFactorAuthViewModel.twoFAVerify(
                                otp = otpCode,
                                walletAddress = address ?: ""
                            )
                        }

                    }
                )
                VSpacer(24.dp)
                subhead2_leah(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.two_factor_dont_have_access)
                )
                VSpacer(2.dp)
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = {
                            navController.slideFromRight(R.id.twoFactorAuthRecoverFragment)
                        })
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center,
                    color = ComposeAppTheme.colors.jacob,
                    style = ComposeAppTheme.typography.subhead2.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    text = stringResource(R.string.two_factor_enter_your_recovery_code)
                )
                VSpacer(50.dp)
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                HSCircularProgressIndicator()
            }
        }
        errorMessage?.let {
            LaunchedEffect(it) {
                HudHelper.showErrorMessage(view, it)
                twoFactorAuthViewModel.isError.value = null
            }
        }
    }
}

@Preview
@Composable
private fun TwoFactorAuthLoginPreview() {
    ComposeAppTheme {
        TwoFactorAuthLoginScreen(
            navController = NavController(LocalContext.current),
            onSuccessfulAuth = {},

            )
    }
}