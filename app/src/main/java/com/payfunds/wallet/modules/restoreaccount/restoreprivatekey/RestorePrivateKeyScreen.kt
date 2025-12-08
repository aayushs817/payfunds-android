package com.payfunds.wallet.modules.restoreaccount.restoreprivatekey

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payfunds.wallet.R
import com.payfunds.wallet.core.stats.StatEntity
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.modules.restoreaccount.RestoreViewModel
import com.payfunds.wallet.modules.restoreaccount.restoremenu.RestoreByMenu
import com.payfunds.wallet.modules.restoreaccount.restoremenu.RestoreMenuViewModel
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.FormsInput
import com.payfunds.wallet.ui.compose.components.FormsInputMultiline
import com.payfunds.wallet.ui.compose.components.HeaderText
import com.payfunds.wallet.ui.compose.components.HsBackButton

@Composable
fun RestorePrivateKey(
    restoreMenuViewModel: RestoreMenuViewModel,
    mainViewModel: RestoreViewModel,
    openSelectCoinsScreen: () -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel =
        viewModel<RestorePrivateKeyViewModel>(factory = RestorePrivateKeyModule.Factory())
    var referralCode by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Restore_Advanced_Title),
                navigationIcon = {
                    HsBackButton(onClick = onBackClick)
                },
//                menuItems = listOf(
//                    MenuItem(
//                        title = TranslatableString.ResString(R.string.Button_Next),
//                        onClick = {
//                            viewModel.resolveAccountType()?.let { accountType ->
//                                mainViewModel.setAccountData(
//                                    accountType,
//                                    viewModel.accountName,
//                                    true,
//                                    false,
//                                    StatPage.ImportWalletFromKeyAdvanced
//                                )
//                                openSelectCoinsScreen.invoke()
//                                viewModel.createAccount(accountType)
//
//                                stat(
//                                    page = StatPage.ImportWalletFromKeyAdvanced,
//                                    event = StatEvent.Open(StatPage.RestoreSelect)
//                                )
//                            }
//                        }
//                    )
//                )
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                ButtonPrimaryRed(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    title = stringResource(R.string.Button_Next),
                    onClick = {
                        viewModel.resolveAccountType()?.let { accountType ->
                            mainViewModel.setAccountData(
                                accountType,
                                viewModel.accountName,
                                true,
                                false,
                                StatPage.ImportWalletFromKeyAdvanced
                            )
                            openSelectCoinsScreen.invoke()
                            viewModel.createAccount(accountType, referralCode = referralCode)

                            stat(
                                page = StatPage.ImportWalletFromKeyAdvanced,
                                event = StatEvent.Open(StatPage.RestoreSelect)
                            )
                        }
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            HeaderText(stringResource(id = R.string.ManageAccount_Name))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                initial = viewModel.accountName,
                pasteEnabled = false,
                hint = viewModel.defaultName,
                onValueChange = viewModel::onEnterName
            )

            Spacer(modifier = Modifier.height(8.dp))
            HeaderText(stringResource(R.string.referral_code))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                initial = referralCode,
                pasteEnabled = false,
                hint = "Referral Code",
                onValueChange = {
                    referralCode = it
                }
            )

            Spacer(Modifier.height(32.dp))

            RestoreByMenu(restoreMenuViewModel)

            Spacer(Modifier.height(32.dp))

            FormsInputMultiline(
                modifier = Modifier.padding(horizontal = 16.dp),
                hint = stringResource(id = R.string.Restore_PrivateKeyHint),
                state = viewModel.inputState,
                qrScannerEnabled = true,
                onValueChange = {
                    viewModel.onEnterPrivateKey(it)
                },
                onClear = {
                    stat(
                        page = StatPage.ImportWalletFromKeyAdvanced,
                        event = StatEvent.Clear(StatEntity.Key)
                    )
                },
                onPaste = {
                    stat(
                        page = StatPage.ImportWalletFromKeyAdvanced,
                        event = StatEvent.Paste(StatEntity.Key)
                    )
                },
                onScanQR = {
                    stat(
                        page = StatPage.ImportWalletFromKeyAdvanced,
                        event = StatEvent.ScanQr(StatEntity.Key)
                    )
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
