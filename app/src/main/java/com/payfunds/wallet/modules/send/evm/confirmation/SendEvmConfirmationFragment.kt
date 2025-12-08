package com.payfunds.wallet.modules.send.evm.confirmation

import android.os.Parcelable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.AppLogger
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.requireInput
import com.payfunds.wallet.core.setNavigationResultX
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.modules.confirm.ConfirmTransactionScreen
import com.payfunds.wallet.modules.send.SendTransactionModule
import com.payfunds.wallet.modules.send.SendTransactionViewModel
import com.payfunds.wallet.modules.send.evm.SendEvmData
import com.payfunds.wallet.modules.send.evm.SendEvmModule
import com.payfunds.wallet.modules.sendevmtransaction.SendEvmTransactionView
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.ethereumkit.models.TransactionData
import io.horizontalsystems.marketkit.models.BlockchainType
import io.payfunds.core.SnackbarDuration
import io.payfunds.core.helpers.HudHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class SendEvmConfirmationFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = try {
            navController.requireInput<Input>()
        } catch (e: NullPointerException) {
            navController.popBackStack()
            return
        }
        SendEvmConfirmationScreen(navController, input)
    }

    @Parcelize
    data class Input(
        val transactionDataParcelable: SendEvmModule.TransactionDataParcelable,
        val additionalInfo: SendEvmData.AdditionalInfo?,
        val blockchainType: BlockchainType,
        val contractAddress: String? = null,
        val amount : String? = null
    ) : Parcelable {
        val transactionData: TransactionData
            get() = TransactionData(
                Address(transactionDataParcelable.toAddress),
                transactionDataParcelable.value,
                transactionDataParcelable.input
            )

        constructor(
            sendData: SendEvmData,
            blockchainType: BlockchainType,
            contractAddress: String?,
            amount: String?
        ) : this(
            SendEvmModule.TransactionDataParcelable(sendData.transactionData),
            sendData.additionalInfo,
            blockchainType,
            contractAddress,
            amount
        )
    }

    @Parcelize
    data class Result(val success: Boolean) : Parcelable
}

@Composable
private fun SendEvmConfirmationScreen(
    navController: NavController,
    input: SendEvmConfirmationFragment.Input
) {
    val logger = remember { AppLogger("send-evm") }

    val currentBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(R.id.sendEvmConfirmationFragment)
    }
    val viewModel = viewModel<SendEvmConfirmationViewModel>(
        viewModelStoreOwner = currentBackStackEntry,
        factory = SendEvmConfirmationViewModel.Factory(
            input.transactionData,
            input.additionalInfo,
            input.blockchainType,
            input.contractAddress,
            input.amount
        )
    )
    val uiState = viewModel.uiState

    val transactionViewModel =
        viewModel<SendTransactionViewModel>(factory = SendTransactionModule.Factory())

    ConfirmTransactionScreen(
        onClickBack = { navController.popBackStack() },
        onClickSettings = {
            navController.slideFromBottom(R.id.sendEvmSettingsFragment)
        },
        onClickClose = null,
        buttonsSlot = {
            val coroutineScope = rememberCoroutineScope()
            val view = LocalView.current

            var buttonEnabled by remember { mutableStateOf(true) }

            ButtonPrimaryRed(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                title = stringResource(R.string.Send_Confirmation_Send_Button),
                onClick = {
                    logger.info("click send button")

                    coroutineScope.launch {
                        buttonEnabled = false
                        HudHelper.showInProcessMessage(
                            view,
                            R.string.Send_Sending,
                            SnackbarDuration.INDEFINITE
                        )

                        val result = try {
                            logger.info("sending tx")
                            val sendData = viewModel.send()

                            transactionViewModel.sendTransactionDetails(
                                contractAddress = viewModel.contractAddress,
                                totalAmount = viewModel.amount ?: "0",
                                toAddress = sendData.fullTransaction.transaction.to.toString(),
                                fromAddress = sendData.fullTransaction.transaction.from.toString(),
                                symbol = uiState.networkFee?.primary?.coinValue?.coin?.code.toString(),
                                txnHash = sendData.fullTransaction.transaction.hashString,
                            )

                            logger.info("success")
                            stat(page = StatPage.SendConfirmation, event = StatEvent.Send)

                            HudHelper.showSuccessMessage(view, R.string.Hud_Text_Done)
                            delay(1200)
                            SendEvmConfirmationFragment.Result(true)
                        } catch (t: Throwable) {
                            logger.warning("failed", t)
                            HudHelper.showErrorMessage(view, t.javaClass.simpleName)
                            SendEvmConfirmationFragment.Result(false)
                        }

                        buttonEnabled = true
                        navController.setNavigationResultX(result)
                        navController.popBackStack()
                    }
                },
                enabled = uiState.sendEnabled && buttonEnabled
            )
        }
    ) {
        SendEvmTransactionView(
            navController,
            uiState.sectionViewItems,
            uiState.cautions,
            uiState.transactionFields,
            uiState.networkFee,
            StatPage.SendConfirmation
        )
    }
}
