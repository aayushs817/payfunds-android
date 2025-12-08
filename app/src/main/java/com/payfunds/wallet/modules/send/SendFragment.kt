package com.payfunds.wallet.modules.send

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseFragment
import com.payfunds.wallet.core.requireInput
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.amount.AmountInputModeModule
import com.payfunds.wallet.modules.amount.AmountInputModeViewModel
import com.payfunds.wallet.modules.send.binance.SendBinanceModule
import com.payfunds.wallet.modules.send.binance.SendBinanceScreen
import com.payfunds.wallet.modules.send.binance.SendBinanceViewModel
import com.payfunds.wallet.modules.send.bitcoin.SendBitcoinModule
import com.payfunds.wallet.modules.send.bitcoin.SendBitcoinNavHost
import com.payfunds.wallet.modules.send.bitcoin.SendBitcoinViewModel
import com.payfunds.wallet.modules.send.evm.SendEvmScreen
import com.payfunds.wallet.modules.send.solana.SendSolanaModule
import com.payfunds.wallet.modules.send.solana.SendSolanaScreen
import com.payfunds.wallet.modules.send.solana.SendSolanaViewModel
import com.payfunds.wallet.modules.send.ton.SendTonModule
import com.payfunds.wallet.modules.send.ton.SendTonScreen
import com.payfunds.wallet.modules.send.ton.SendTonViewModel
import com.payfunds.wallet.modules.send.tron.SendTronModule
import com.payfunds.wallet.modules.send.tron.SendTronScreen
import com.payfunds.wallet.modules.send.tron.SendTronViewModel
import com.payfunds.wallet.modules.send.zcash.SendZCashModule
import com.payfunds.wallet.modules.send.zcash.SendZCashScreen
import com.payfunds.wallet.modules.send.zcash.SendZCashViewModel
import com.payfunds.wallet.modules.sendtokenselect.PrefilledData
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenType
import io.payfunds.core.findNavController
import kotlinx.parcelize.Parcelize

class SendFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            try {
                val navController = findNavController()
                val input = navController.requireInput<Input>()
                val wallet = input.wallet
                val title = input.title
                val sendEntryPointDestId = input.sendEntryPointDestId
                val predefinedAddress = input.predefinedAddress
                val prefilledData = input.prefilledAddressData
                val contractAddress = when (wallet.token.type) {
                    is TokenType.AddressTyped -> null
                    is TokenType.Bep2 -> null
                    is TokenType.Derived -> null
                    is TokenType.Native -> null
                    is TokenType.Eip20 -> {
                        (wallet.token.type as TokenType.Eip20).address
                    }

                    is TokenType.Jetton -> (wallet.token.type as TokenType.Jetton).address
                    is TokenType.Spl -> {
                        (wallet.token.type as TokenType.Spl).address
                    }

                    is TokenType.Unsupported -> {
                        (wallet.token.type as TokenType.Unsupported).reference
                    }
                }

                val amountInputModeViewModel by navGraphViewModels<AmountInputModeViewModel>(R.id.sendXFragment) {
                    AmountInputModeModule.Factory(wallet.coin.uid)
                }

                when (wallet.token.blockchainType) {
                    BlockchainType.Bitcoin,
                    BlockchainType.BitcoinCash,
                    BlockchainType.ECash,
                    BlockchainType.Litecoin,
                    BlockchainType.Dash -> {
                        val factory = SendBitcoinModule.Factory(wallet, predefinedAddress)
                        val sendBitcoinViewModel by navGraphViewModels<SendBitcoinViewModel>(R.id.sendXFragment) {
                            factory
                        }
                        setContent {
                            SendBitcoinNavHost(
                                title,
                                findNavController(),
                                sendBitcoinViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                prefilledData,
                            )
                        }
                    }

                    is BlockchainType.BinanceChain -> {
                        val factory = SendBinanceModule.Factory(wallet, predefinedAddress)
                        val sendBinanceViewModel by navGraphViewModels<SendBinanceViewModel>(R.id.sendXFragment) {
                            factory
                        }
                        setContent {
                            SendBinanceScreen(
                                title,
                                findNavController(),
                                sendBinanceViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                prefilledData,
                            )
                        }
                    }

                    BlockchainType.Zcash -> {
                        val factory = SendZCashModule.Factory(wallet, predefinedAddress)
                        val sendZCashViewModel by navGraphViewModels<SendZCashViewModel>(R.id.sendXFragment) {
                            factory
                        }
                        setContent {
                            SendZCashScreen(
                                title,
                                findNavController(),
                                sendZCashViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                prefilledData,
                            )
                        }
                    }

                    BlockchainType.Ethereum,
                    BlockchainType.BinanceSmartChain,
                    BlockchainType.Polygon,
                    BlockchainType.Avalanche,
                    BlockchainType.Optimism,
                    BlockchainType.Base,
                    BlockchainType.Gnosis,
                    BlockchainType.Fantom,
                    BlockchainType.ArbitrumOne -> {
                        setContent {
                            SendEvmScreen(
                                title,
                                findNavController(),
                                amountInputModeViewModel,
                                prefilledData,
                                wallet,
                                contractAddress,
                                predefinedAddress
                            )
                        }
                    }

                    BlockchainType.Solana -> {
                        val factory = SendSolanaModule.Factory(wallet, predefinedAddress)
                        val sendSolanaViewModel by navGraphViewModels<SendSolanaViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendSolanaScreen(
                                title,
                                findNavController(),
                                sendSolanaViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                prefilledData,
                            )
                        }
                    }

                    BlockchainType.Ton -> {
                        val factory = SendTonModule.Factory(wallet, predefinedAddress)
                        val sendTonViewModel by navGraphViewModels<SendTonViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendTonScreen(
                                title,
                                findNavController(),
                                sendTonViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                prefilledData,
                            )
                        }
                    }

                    BlockchainType.Tron -> {
                        val transactionViewModel: SendTransactionViewModel by viewModels {
                            SendTransactionModule.Factory()
                        }

                        val factory =
                            SendTronModule.Factory(wallet, predefinedAddress, transactionViewModel)
                        val sendTronViewModel by navGraphViewModels<SendTronViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendTronScreen(
                                title,
                                findNavController(),
                                sendTronViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                prefilledData,
                            )
                        }
                    }

                    else -> {}
                }
            } catch (t: Throwable) {
                findNavController().popBackStack()
            }
        }
    }

    @Parcelize
    data class Input(
        val wallet: Wallet,
        val title: String,
        val sendEntryPointDestId: Int = 0,
        val predefinedAddress: String? = null,
        val prefilledAddressData: PrefilledData? = null
    ) : Parcelable
}
