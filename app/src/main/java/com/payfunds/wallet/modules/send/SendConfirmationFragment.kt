package com.payfunds.wallet.modules.send

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.modules.amount.AmountInputModeViewModel
import com.payfunds.wallet.modules.send.binance.SendBinanceConfirmationScreen
import com.payfunds.wallet.modules.send.binance.SendBinanceViewModel
import com.payfunds.wallet.modules.send.bitcoin.SendBitcoinConfirmationScreen
import com.payfunds.wallet.modules.send.bitcoin.SendBitcoinViewModel
import com.payfunds.wallet.modules.send.solana.SendSolanaConfirmationScreen
import com.payfunds.wallet.modules.send.solana.SendSolanaViewModel
import com.payfunds.wallet.modules.send.ton.SendTonConfirmationScreen
import com.payfunds.wallet.modules.send.ton.SendTonViewModel
import com.payfunds.wallet.modules.send.tron.SendTronConfirmationScreen
import com.payfunds.wallet.modules.send.tron.SendTronViewModel
import com.payfunds.wallet.modules.send.zcash.SendZCashConfirmationScreen
import com.payfunds.wallet.modules.send.zcash.SendZCashViewModel
import kotlinx.parcelize.Parcelize

class SendConfirmationFragment : BaseComposeFragment() {
    val amountInputModeViewModel by navGraphViewModels<AmountInputModeViewModel>(R.id.sendXFragment)

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()
        val sendEntryPointDestId = input?.sendEntryPointDestId ?: 0

        when (input?.type) {
            Type.Bitcoin -> {
                val sendBitcoinViewModel by navGraphViewModels<SendBitcoinViewModel>(R.id.sendXFragment)

                SendBitcoinConfirmationScreen(
                    navController,
                    sendBitcoinViewModel,
                    amountInputModeViewModel,
                    sendEntryPointDestId
                )
            }

            Type.Bep2 -> {
                val sendBinanceViewModel by navGraphViewModels<SendBinanceViewModel>(R.id.sendXFragment)

                SendBinanceConfirmationScreen(
                    navController,
                    sendBinanceViewModel,
                    amountInputModeViewModel,
                    sendEntryPointDestId
                )
            }

            Type.ZCash -> {
                val sendZCashViewModel by navGraphViewModels<SendZCashViewModel>(R.id.sendXFragment)

                SendZCashConfirmationScreen(
                    navController,
                    sendZCashViewModel,
                    amountInputModeViewModel,
                    sendEntryPointDestId
                )
            }

            Type.Tron -> {
                val sendTronViewModel by navGraphViewModels<SendTronViewModel>(R.id.sendXFragment)
                SendTronConfirmationScreen(
                    navController,
                    sendTronViewModel,
                    amountInputModeViewModel,
                    sendEntryPointDestId
                )
            }

            Type.Solana -> {
                val sendSolanaViewModel by navGraphViewModels<SendSolanaViewModel>(R.id.sendXFragment)

                SendSolanaConfirmationScreen(
                    navController,
                    sendSolanaViewModel,
                    amountInputModeViewModel,
                    sendEntryPointDestId
                )
            }

            Type.Ton -> {
                val sendTonViewModel by navGraphViewModels<SendTonViewModel>(R.id.sendXFragment)

                SendTonConfirmationScreen(
                    navController,
                    sendTonViewModel,
                    amountInputModeViewModel,
                    sendEntryPointDestId
                )
            }

            null -> Unit
        }
    }

    @Parcelize
    enum class Type : Parcelable {
        Bitcoin, Bep2, ZCash, Solana, Tron, Ton
    }

    @Parcelize
    data class Input(val type: Type, val sendEntryPointDestId: Int) : Parcelable
}
