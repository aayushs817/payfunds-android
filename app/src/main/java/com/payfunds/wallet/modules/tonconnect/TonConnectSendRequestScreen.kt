package com.payfunds.wallet.modules.tonconnect

import androidx.activity.ComponentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.AppLogger
import com.payfunds.wallet.core.adapters.TonTransactionRecord
import com.payfunds.wallet.modules.amount.AmountInputType
import com.payfunds.wallet.modules.confirm.ConfirmTransactionScreen
import com.payfunds.wallet.modules.fee.HSFeeRaw
import com.payfunds.wallet.modules.main.MainActivityViewModel
import com.payfunds.wallet.modules.multiswap.TokenRowPure
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryDefault
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.payfunds.wallet.ui.compose.components.HFillSpacer
import com.payfunds.wallet.ui.compose.components.TextImportantError
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.cell.CellUniversal
import com.payfunds.wallet.ui.compose.components.cell.SectionUniversalLawrence
import com.payfunds.wallet.ui.compose.components.subhead1_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import io.payfunds.core.SnackbarDuration
import io.payfunds.core.helpers.HudHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TonConnectSendRequestScreen(navController: NavController) {
    val logger = remember { AppLogger("ton-connect request") }
    val mainActivityViewModel =
        viewModel<MainActivityViewModel>(viewModelStoreOwner = LocalContext.current as ComponentActivity)
    val viewModel = viewModel<TonConnectSendRequestViewModel>(initializer = {
        val sendRequestEntity = mainActivityViewModel.tcSendRequest.value
        mainActivityViewModel.onTcSendRequestHandled()

        TonConnectSendRequestViewModel(
            sendRequestEntity,
            App.accountManager,
            App.tonConnectManager
        )
    })

    val uiState = viewModel.uiState

    ConfirmTransactionScreen(
        onClickBack = navController::popBackStack,
        onClickSettings = null,
        onClickClose = null,
        buttonsSlot = {
            val coroutineScope = rememberCoroutineScope()
            val view = LocalView.current

            var buttonEnabled by remember { mutableStateOf(true) }

            ButtonPrimaryRed(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.Button_Confirm),
                enabled = uiState.confirmEnabled && buttonEnabled,
                onClick = {
                    coroutineScope.launch {
                        buttonEnabled = false
                        HudHelper.showInProcessMessage(
                            view,
                            R.string.Send_Sending,
                            SnackbarDuration.INDEFINITE
                        )

                        try {
                            logger.info("click confirm button")
                            viewModel.confirm()
                            logger.info("success")

                            HudHelper.showSuccessMessage(view, R.string.Hud_Text_Done)
                            delay(1200)
                        } catch (t: Throwable) {
                            logger.warning("failed", t)
                            HudHelper.showErrorMessage(view, t.javaClass.simpleName)
                        }

                        buttonEnabled = true
                        navController.popBackStack()
                    }
                }
            )
            VSpacer(16.dp)
            ButtonPrimaryDefault(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.Button_Reject),
                enabled = uiState.rejectEnabled,
                onClick = {
                    viewModel.reject()
                    navController.popBackStack()
                }
            )
        }
    ) {
        uiState.error?.let { error ->
            TextImportantError(text = error.message ?: error.javaClass.simpleName)
        }

        Crossfade(uiState.tonTransactionRecord) { transactionRecord ->
            Column {
                if (transactionRecord != null) {
                    SectionUniversalLawrence {
                        transactionRecord.actions.forEach { action ->
                            when (val actionType = action.type) {
                                is TonTransactionRecord.Action.Type.Swap -> {
                                    val valueIn = actionType.valueIn
                                    val valueInDecimalValue = valueIn.decimalValue
                                    val valueInDecimals = valueIn.decimals
                                    val amountFormatted =
                                        if (valueInDecimalValue != null && valueInDecimals != null) {
                                            App.numberFormatter.formatCoinFull(
                                                valueInDecimalValue.abs(),
                                                valueIn.coinCode,
                                                valueInDecimals
                                            )
                                        } else {
                                            null
                                        }
                                    TokenRowPure(
                                        fiatAmount = null,
                                        borderTop = false,
                                        currency = uiState.currency,
                                        title = stringResource(R.string.Send_Confirmation_YouSend),
                                        amountColor = ComposeAppTheme.colors.leah,
                                        imageUrl = valueIn.coinIconUrl,
                                        alternativeImageUrl = valueIn.alternativeCoinIconUrl,
                                        imagePlaceholder = valueIn.coinIconPlaceholder,
                                        badge = valueIn.badge,
                                        amountFormatted = amountFormatted
                                    )

                                    val valueOut = actionType.valueOut
                                    val valueOutDecimalValue = valueOut.decimalValue
                                    val valueOutDecimals = valueOut.decimals
                                    val valueOutAmountFormatted =
                                        if (valueOutDecimalValue != null && valueOutDecimals != null) {
                                            App.numberFormatter.formatCoinFull(
                                                valueOutDecimalValue,
                                                valueOut.coinCode,
                                                valueOutDecimals
                                            )
                                        } else {
                                            null
                                        }

                                    TokenRowPure(
                                        fiatAmount = null,
                                        currency = uiState.currency,
                                        title = stringResource(R.string.Swap_ToAmountTitle),
                                        amountColor = ComposeAppTheme.colors.remus,
                                        imageUrl = valueOut.coinIconUrl,
                                        alternativeImageUrl = valueOut.alternativeCoinIconUrl,
                                        imagePlaceholder = valueOut.coinIconPlaceholder,
                                        badge = valueOut.badge,
                                        amountFormatted = valueOutAmountFormatted,
                                    )
                                }
//                                is TonTransactionRecord.Action.Type.Burn -> {}
//                                is TonTransactionRecord.Action.Type.ContractCall -> {}
//                                is TonTransactionRecord.Action.Type.ContractDeploy -> {}
//                                is TonTransactionRecord.Action.Type.Mint -> {}
//                                is TonTransactionRecord.Action.Type.Receive -> {}
//                                is TonTransactionRecord.Action.Type.Send -> {}
//                                is TonTransactionRecord.Action.Type.Unsupported -> {}
                                else -> {
                                    CellUniversal(borderTop = false) {
                                        subhead2_grey(text = stringResource(R.string.Send_Confirmation_Action))
                                        HFillSpacer(minWidth = 16.dp)
                                        subhead1_leah(text = actionType.javaClass.simpleName)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    val fee = transactionRecord.fee
                    CellUniversalLawrenceSection(
                        listOf {
                            HSFeeRaw(
                                coinCode = fee.coinCode,
                                coinDecimal = fee.decimals,
                                fee = fee.value,
                                amountInputType = AmountInputType.COIN,
                                rate = null,
                                navController = navController
                            )
                        }
                    )
                }
            }
        }
    }
}
