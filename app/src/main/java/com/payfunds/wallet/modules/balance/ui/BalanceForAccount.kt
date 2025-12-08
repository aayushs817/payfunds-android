package com.payfunds.wallet.modules.balance.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.Caution
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.core.utils.ModuleField
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.backupalert.BackupAlert
import com.payfunds.wallet.modules.balance.AccountViewItem
import com.payfunds.wallet.modules.balance.BalanceModule
import com.payfunds.wallet.modules.balance.BalanceViewModel
import com.payfunds.wallet.modules.coin.overview.ui.Loading
import com.payfunds.wallet.modules.contacts.screen.ConfirmationBottomSheet
import com.payfunds.wallet.modules.manageaccount.dialogs.BackupRequiredDialog
import com.payfunds.wallet.modules.manageaccounts.ManageAccountsModule
import com.payfunds.wallet.modules.qrscanner.QRScannerActivity
import com.payfunds.wallet.modules.settings.security.twofactorauth.TwoFactorAuthLoginScreen
import com.payfunds.wallet.modules.walletconnect.WCAccountTypeNotSupportedDialog
import com.payfunds.wallet.modules.walletconnect.WCManager
import com.payfunds.wallet.modules.walletconnect.list.WalletConnectListViewModel
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.HSCircularProgressIndicator
import com.payfunds.wallet.ui.compose.components.MenuItem
import com.payfunds.wallet.ui.compose.components.RippleRadius
import io.payfunds.core.helpers.HudHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BalanceForAccount(navController: NavController, accountViewItem: AccountViewItem) {
    val viewModel = viewModel<BalanceViewModel>(factory = BalanceModule.Factory())

    val context = LocalContext.current
    val invalidUrlBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val qrScannerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.handleScannedData(
                    result.data?.getStringExtra(ModuleField.SCAN_ADDRESS) ?: ""
                )
            }
        }

    viewModel.uiState.errorMessage?.let { message ->
        val view = LocalView.current
        HudHelper.showErrorMessage(view, text = message)
        viewModel.errorShown()
        viewModel.apiError.value = null
    }

    when (viewModel.connectionResult) {
        WalletConnectListViewModel.ConnectionResult.Error -> {
            LaunchedEffect(viewModel.connectionResult) {
                coroutineScope.launch {
                    delay(300)
                    invalidUrlBottomSheetState.show()
                }
            }
            viewModel.onHandleRoute()
        }

        else -> Unit
    }

    BackupAlert(navController)

    val isMultiFactorEnabled by DashboardObject.isMultiFactor.collectAsState()

    LaunchedEffect(viewModel.createUserResponseModel.value) {
        viewModel.createUserResponseModel.value?.let {
            DashboardObject.updateIsMultiFactor(it.data.isMultiFactor)
            DashboardObject.update2FAEnabled(it.data.isMultiFactor)
            viewModel.createUserResponseModel.value = null
        }
    }

    LaunchedEffect(true) {
        viewModel.isUserApiCalled = false
    }

    if (viewModel.isApiLoading.value) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            HSCircularProgressIndicator()
        }
    }
    isMultiFactorEnabled?.let {
        if (!it) {
            ModalBottomSheetLayout(
                sheetState = invalidUrlBottomSheetState,
                sheetBackgroundColor = ComposeAppTheme.colors.transparent,
                sheetContent = {
                    ConfirmationBottomSheet(
                        title = stringResource(R.string.WalletConnect_Title),
                        text = stringResource(R.string.WalletConnect_Error_InvalidUrl),
                        iconPainter = painterResource(R.drawable.ic_wallet_connect_24),
                        iconTint = ColorFilter.tint(ComposeAppTheme.colors.jacob),
                        confirmText = stringResource(R.string.Button_TryAgain),
                        cautionType = Caution.Type.Warning,
                        cancelText = stringResource(R.string.Button_Cancel),
                        onConfirm = {
                            coroutineScope.launch {
                                invalidUrlBottomSheetState.hide()
                                qrScannerLauncher.launch(
                                    QRScannerActivity.getScanQrIntent(
                                        context,
                                        true
                                    )
                                )
                            }
                        },
                        onClose = {
                            coroutineScope.launch { invalidUrlBottomSheetState.hide() }
                        }
                    )
                }
            ) {
                Scaffold(
        modifier = Modifier.statusBarsPadding(),
                    backgroundColor = ComposeAppTheme.colors.tyler,
                    topBar = {
                        AppBar(
                            title = {
                                BalanceTitleRow(navController, accountViewItem.name)
                            },
                            menuItems = buildList {
                                add(
                                    MenuItem(
                                        title = TranslatableString.ResString(R.string.notifications),
                                        icon = R.drawable.ic_notification,
                                        tint = ComposeAppTheme.colors.redG,
                                        onClick = {
                                            navController.slideFromRight(R.id.notificationFragment)
                                        }
                                    )
                                )

                                if (accountViewItem.type.supportsWalletConnect) {
                                    add(
                                        MenuItem(
                                            title = TranslatableString.ResString(R.string.WalletConnect_NewConnect),
                                            icon = R.drawable.ic_qr_scan_20,
                                            onClick = {
                                                when (val state =
                                                    viewModel.getWalletConnectSupportState()) {
                                                    WCManager.SupportState.Supported -> {
                                                        qrScannerLauncher.launch(
                                                            QRScannerActivity.getScanQrIntent(
                                                                context,
                                                                true
                                                            )
                                                        )

                                                        stat(
                                                            page = StatPage.Balance,
                                                            event = StatEvent.Open(StatPage.ScanQrCode)
                                                        )
                                                    }

                                                    WCManager.SupportState.NotSupportedDueToNoActiveAccount -> {
                                                        navController.slideFromBottom(R.id.wcErrorNoAccountFragment)
                                                    }

                                                    is WCManager.SupportState.NotSupportedDueToNonBackedUpAccount -> {
                                                        val text =
                                                            Translator.getString(R.string.WalletConnect_Error_NeedBackup)
                                                        navController.slideFromBottom(
                                                            R.id.backupRequiredDialog,
                                                            BackupRequiredDialog.Input(
                                                                state.account,
                                                                text
                                                            )
                                                        )

                                                        stat(
                                                            page = StatPage.Balance,
                                                            event = StatEvent.Open(StatPage.BackupRequired)
                                                        )
                                                    }

                                                    is WCManager.SupportState.NotSupported -> {
                                                        navController.slideFromBottom(
                                                            R.id.wcAccountTypeNotSupportedDialog,
                                                            WCAccountTypeNotSupportedDialog.Input(
                                                                state.accountTypeDescription
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    val uiState = viewModel.uiState

                    Column {
                        Crossfade(
                            targetState = uiState.viewState,
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize(),
                            label = ""
                        ) { viewState ->
                            when (viewState) {
                                ViewState.Success -> {
                                    val balanceViewItems = uiState.balanceViewItems

                                    BalanceItems(
                                        balanceViewItems,
                                        viewModel,
                                        accountViewItem,
                                        navController,
                                        uiState,
                                        viewModel.totalUiState,
                                    )
                                }

                                ViewState.Loading -> {
                                    Loading()
                                }

                                is ViewState.Error,
                                null -> {
                                }
                            }
                        }
                    }
                }
            }
        } else {

            TwoFactorAuthLoginScreen(navController, onSuccessfulAuth = {
                DashboardObject.updateIsMultiFactor(it)

            })
        }
    }

}

@Composable
fun BalanceTitleRow(
    navController: NavController,
    title: String
) {
    Box(
        modifier = Modifier
            .defaultMinSize(24.dp)
            .clickable(
                onClick = {
                    navController.slideFromBottom(
                        R.id.manageAccountsFragment,
                        ManageAccountsModule.Mode.Switcher
                    )
                    stat(page = StatPage.Balance, event = StatEvent.Open(StatPage.ManageWallets))
                },
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    radius = RippleRadius,
                    color = ComposeAppTheme.colors.leah
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_wallet_24),
            contentDescription = title,
            tint = ComposeAppTheme.colors.redG
        )
    }
}