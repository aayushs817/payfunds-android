package com.payfunds.wallet.modules.walletconnect.request.signtransaction

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.R
import com.payfunds.wallet.core.AppLogger
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.modules.confirm.ConfirmTransactionScreen
import com.payfunds.wallet.modules.sendevmtransaction.SendEvmTransactionView
import com.payfunds.wallet.modules.walletconnect.request.sendtransaction.WalletConnectTransaction
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryDefault
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.VSpacer
import io.payfunds.core.helpers.HudHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WCSignEthereumTransactionRequestScreen(
    navController: NavController,
    logger: AppLogger,
    blockchainType: BlockchainType,
    transaction: WalletConnectTransaction,
    peerName: String,
) {
    val viewModelStoreOwner = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(R.id.wcRequestFragment)
    }
    val viewModel = viewModel<WCSignEthereumTransactionRequestViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = WCSignEthereumTransactionRequestViewModel.Factory(
            blockchainType = blockchainType,
            transaction = transaction,
            peerName = peerName
        )
    )
    val uiState = viewModel.uiState

    ConfirmTransactionScreen(
        title = stringResource(id = R.string.WalletConnect_SignMessageRequest_Title),
        onClickBack = navController::popBackStack,
        onClickSettings = null,
        onClickClose = navController::popBackStack,
        buttonsSlot = {
            val coroutineScope = rememberCoroutineScope()
            val view = LocalView.current


            ButtonPrimaryRed(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.Button_Sign),
                onClick = {
                    coroutineScope.launch {
                        try {
                            logger.info("click sign button")
                            viewModel.sign()
                            logger.info("success")

                            HudHelper.showSuccessMessage(view, R.string.Hud_Text_Done)
                            delay(1200)
                        } catch (t: Throwable) {
                            logger.warning("failed", t)
                            HudHelper.showErrorMessage(view, t.javaClass.simpleName)
                        }

                        navController.popBackStack()
                    }
                }
            )
            VSpacer(16.dp)
            ButtonPrimaryDefault(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.Button_Reject),
                onClick = {
                    viewModel.reject()
                    navController.popBackStack()
                }
            )
        }
    ) {
        SendEvmTransactionView(
            navController,
            uiState.sectionViewItems,
            uiState.cautions,
            uiState.transactionFields,
            uiState.networkFee,
            StatPage.WalletConnect
        )
    }
}
