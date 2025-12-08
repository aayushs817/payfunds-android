package com.payfunds.wallet.modules.send.ton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.ISendTonAdapter
import com.payfunds.wallet.core.isNative
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.amount.AmountValidator
import com.payfunds.wallet.modules.xrate.XRateService

object SendTonModule {
    class Factory(
        private val wallet: Wallet,
        private val predefinedAddress: String?,
    ) : ViewModelProvider.Factory {
        val adapter = (App.adapterManager.getAdapterForWallet(wallet) as? ISendTonAdapter)
            ?: throw IllegalStateException("ISendTonAdapter is null")

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                SendTonViewModel::class.java -> {
                    val amountValidator = AmountValidator()
                    val coinMaxAllowedDecimals = wallet.token.decimals

                    val amountService = SendTonAmountService(
                        amountValidator = amountValidator,
                        coinCode = wallet.coin.code,
                        availableBalance = adapter.availableBalance,
                        leaveSomeBalanceForFee = wallet.token.type.isNative
                    )
                    val addressService = SendTonAddressService(predefinedAddress)
                    val feeService = SendTonFeeService(adapter)
                    val xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency)
                    val feeToken =
                        App.coinManager.getToken(TokenQuery(BlockchainType.Ton, TokenType.Native))
                            ?: throw IllegalArgumentException()

                    SendTonViewModel(
                        wallet,
                        wallet.token,
                        feeToken,
                        adapter,
                        xRateService,
                        amountService,
                        addressService,
                        feeService,
                        coinMaxAllowedDecimals,
                        App.contactsRepository,
                        predefinedAddress == null
                    ) as T
                }

                else -> throw IllegalArgumentException()
            }
        }
    }

}


