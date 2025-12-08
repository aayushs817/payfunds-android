package com.payfunds.wallet.modules.sendtokenselect

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenType
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.modules.send.SendFragment
import com.payfunds.wallet.modules.tokenselect.TokenSelectScreen
import com.payfunds.wallet.modules.tokenselect.TokenSelectViewModel
import io.payfunds.core.helpers.HudHelper
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

class SendTokenSelectFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()

        val blockchainTypes = input?.blockchainTypes
        val tokenTypes = input?.tokenTypes
        val prefilledData = input?.prefilledData
        val view = LocalView.current
        TokenSelectScreen(
            navController = navController,
            title = stringResource(R.string.Balance_Send),
            searchHintText = stringResource(R.string.Balance_SendHint_CoinName),
            onClickItem = {
                when {
                    it.sendEnabled -> {
                        val sendTitle = Translator.getString(
                            R.string.Send_Title,
                            it.wallet.token.fullCoin.coin.code
                        )
                        navController.slideFromRight(
                            R.id.sendXFragment,
                            SendFragment.Input(
                                wallet = it.wallet,
                                sendEntryPointDestId = R.id.sendTokenSelectFragment,
                                title = sendTitle,
                                prefilledAddressData = prefilledData,
                            )
                        )
                    }

                    it.syncingProgress.progress != null -> {
                        HudHelper.showWarningMessage(view, R.string.Hud_WaitForSynchronization)
                    }

                    it.errorMessage != null -> {
                        HudHelper.showErrorMessage(view, it.errorMessage)
                    }
                }
            },
            viewModel = viewModel(
                factory = TokenSelectViewModel.FactoryForSend(
                    blockchainTypes,
                    tokenTypes
                )
            ),
            emptyItemsText = stringResource(R.string.Balance_NoAssetsToSend)
        )
    }

    @Parcelize
    data class Input(
        val blockchainTypes: List<BlockchainType>?,
        val tokenTypes: List<TokenType>?,
        val address: String,
        val amount: BigDecimal?,
    ) : Parcelable {
        val prefilledData: PrefilledData
            get() = PrefilledData(address, amount)
    }
}

@Parcelize
data class PrefilledData(
    val address: String,
    val amount: BigDecimal? = null,
) : Parcelable