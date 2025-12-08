package com.payfunds.wallet.modules.settings.security.twofactorauth

import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.network.response_model.two_factor_auth.qr.RecoveryCode
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.FormsInput
import com.payfunds.wallet.ui.compose.components.HSCircularProgressIndicator
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.compose.components.subhead2_jacob
import io.payfunds.core.helpers.HudHelper

class TwoFactorAuthEnableFragment : BaseComposeFragment() {

    private val viewModel by viewModels<TwoFactorAuthViewModel> {
        TwoFactorAuthModule.Factory()
    }

    @Composable
    override fun GetContent(navController: NavController) {
        TwoFactorAuthEnableScreen(
            close = { navController.popBackStack() },
            viewModel = viewModel,
            navController = navController
        )
    }
}


@Composable
private fun TwoFactorAuthEnableScreen(
    close: () -> Unit,
    viewModel: TwoFactorAuthViewModel,
    navController: NavController
) {
    val enabledResponse by viewModel.twoFAEnableResponse
    val qrResponse by viewModel.twoFAGenerateQRResponse
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.isError
    val view = LocalView.current
    var otpCode by remember { mutableStateOf("") }
    var walletAddress by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val account = App.accountManager.activeAccount
        account?.let {
            val address = getWalletAddress(account)
            walletAddress = address ?: ""
        }
    }

    LaunchedEffect(Unit) {
        viewModel.twoFAGenerateQRCode()
    }

    enabledResponse?.let {
        if (it.isSuccess) {
            DashboardObject.updateIsDashboardOpened(true)
            DashboardObject.update2FAEnabled(true)
            val arguments = qrResponse?.data?.recoveryCode?.let { code -> RecoveryCode(code) }
            navController.slideFromRight(R.id.twoFactorAuthRecoverCodeFragment, arguments)
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
                ButtonPrimaryRed(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = otpCode.length == 6,
                    title = stringResource(R.string.confirm),
                    onClick = {
                        viewModel.twoFAEnable(otp = otpCode)
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                subhead2_grey(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    text = stringResource(R.string.Appearance_Google_Authenticator_App),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .size(180.dp)
                        .background(color = ComposeAppTheme.colors.lawrence),
                    contentAlignment = Alignment.Center,
                ) {
                    qrResponse?.data?.let {
                        LoadBase64Image(it.secretUri)
                    }
                }
                subhead2_grey(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    text = stringResource(R.string.Appearance_Please_enter_the_code),
                    textAlign = TextAlign.Center
                )

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
                qrResponse?.data?.let {
                    subhead2_jacob(
                        modifier = Modifier
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {
                                clipboardManager.setText(AnnotatedString(it.secret))
                            })
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        text = "Setup Key: ${it.secret}",
                        textAlign = TextAlign.Center
                    )
                }
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
private fun TwoFactorAuthGenerateQRPreview() {
    ComposeAppTheme {
        TwoFactorAuthEnableScreen(
            close = {},
            viewModel = viewModel(),
            navController = NavController(LocalContext.current),
        )
    }
}


@Composable
fun LoadBase64Image(
    base64ImageStringWithPrefix: String,
    isLoading: Boolean = false,
) {
    val base64ImageString = base64ImageStringWithPrefix.substringAfter("base64,")
    val imageByteArray = Base64.decode(base64ImageString, Base64.DEFAULT)
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(data = imageByteArray)
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
            }).build()
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = "Base64 Image",
            modifier = Modifier.fillMaxSize()
        )
        if (isLoading) {
            HSCircularProgressIndicator()
        }
    }
}
