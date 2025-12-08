package com.payfunds.wallet.modules.send.binance

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.entities.Address
import com.payfunds.wallet.modules.address.AddressParserModule
import com.payfunds.wallet.modules.address.AddressParserViewModel
import com.payfunds.wallet.modules.address.HSAddressInput
import com.payfunds.wallet.modules.amount.AmountInputModeViewModel
import com.payfunds.wallet.modules.amount.HSAmountInput
import com.payfunds.wallet.modules.availablebalance.AvailableBalance
import com.payfunds.wallet.modules.fee.HSFee
import com.payfunds.wallet.modules.memo.HSMemoInput
import com.payfunds.wallet.modules.send.SendConfirmationFragment
import com.payfunds.wallet.modules.send.SendScreen
import com.payfunds.wallet.modules.send.bitcoin.advanced.FeeRateCaution
import com.payfunds.wallet.modules.sendtokenselect.PrefilledData
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed

@Composable
fun SendBinanceScreen(
    title: String,
    navController: NavController,
    viewModel: SendBinanceViewModel,
    amountInputModeViewModel: AmountInputModeViewModel,
    sendEntryPointDestId: Int,
    prefilledData: PrefilledData?,
) {
    val wallet = viewModel.wallet
    val uiState = viewModel.uiState

    val availableBalance = uiState.availableBalance
    val addressError = uiState.addressError
    val amountCaution = uiState.amountCaution
    val fee = uiState.fee
    val proceedEnabled = uiState.canBeSend
    val amountInputType = amountInputModeViewModel.inputType

    val paymentAddressViewModel = viewModel<AddressParserViewModel>(
        factory = AddressParserModule.Factory(wallet.token, prefilledData?.amount)
    )
    val amountUnique = paymentAddressViewModel.amountUnique

    ComposeAppTheme {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        SendScreen(
            title = title,
            onCloseClick = { navController.popBackStack() }
        ) {
            AvailableBalance(
                coinCode = wallet.coin.code,
                coinDecimal = viewModel.coinMaxAllowedDecimals,
                fiatDecimal = viewModel.fiatMaxAllowedDecimals,
                availableBalance = availableBalance,
                amountInputType = amountInputType,
                rate = viewModel.coinRate
            )

            Spacer(modifier = Modifier.height(12.dp))
            HSAmountInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                focusRequester = focusRequester,
                availableBalance = availableBalance,
                caution = amountCaution,
                coinCode = wallet.coin.code,
                coinDecimal = viewModel.coinMaxAllowedDecimals,
                fiatDecimal = viewModel.fiatMaxAllowedDecimals,
                onClickHint = {
                    amountInputModeViewModel.onToggleInputType()
                },
                onValueChange = {
                    viewModel.onEnterAmount(it)
                },
                inputType = amountInputType,
                rate = viewModel.coinRate,
                amountUnique = amountUnique
            )

            if (uiState.showAddressInput) {
                Spacer(modifier = Modifier.height(12.dp))
                HSAddressInput(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    initial = prefilledData?.address?.let { Address(it) },
                    tokenQuery = wallet.token.tokenQuery,
                    coinCode = wallet.coin.code,
                    error = addressError,
                    textPreprocessor = paymentAddressViewModel,
                    navController = navController
                ) {
                    viewModel.onEnterAddress(it)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HSMemoInput(
                maxLength = viewModel.memoMaxLength
            ) {
                viewModel.onEnterMemo(it)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HSFee(
                coinCode = viewModel.feeToken.coin.code,
                coinDecimal = viewModel.feeTokenMaxAllowedDecimals,
                fee = fee,
                amountInputType = amountInputType,
                rate = viewModel.feeCoinRate,
                navController = navController
            )

            uiState.feeCaution?.let { caution ->
                FeeRateCaution(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    feeRateCaution = caution
                )
            }

            ButtonPrimaryRed(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                title = stringResource(R.string.Send_DialogProceed),
                onClick = {
                    navController.slideFromRight(
                        R.id.sendConfirmation,
                        SendConfirmationFragment.Input(
                            SendConfirmationFragment.Type.Bep2,
                            sendEntryPointDestId
                        )
                    )
                },
                enabled = proceedEnabled
            )
        }
    }
}
