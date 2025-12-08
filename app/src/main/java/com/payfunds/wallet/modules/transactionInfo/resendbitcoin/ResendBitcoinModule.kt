package com.payfunds.wallet.modules.transactionInfo.resendbitcoin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.adapters.BitcoinBaseAdapter
import com.payfunds.wallet.core.factories.FeeRateProviderFactory
import com.payfunds.wallet.entities.transactionrecords.bitcoin.BitcoinOutgoingTransactionRecord
import com.payfunds.wallet.modules.transactionInfo.options.SpeedUpCancelType
import com.payfunds.wallet.modules.transactions.TransactionSource
import com.payfunds.wallet.modules.xrate.XRateService

object ResendBitcoinModule {

    class Factory(
        private val optionType: SpeedUpCancelType,
        private val transactionRecord: BitcoinOutgoingTransactionRecord,
        private val source: TransactionSource
    ) : ViewModelProvider.Factory {

        private val adapter by lazy {
            App.transactionAdapterManager.getAdapter(source) as BitcoinBaseAdapter
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val replacementInfo = when (optionType) {
                SpeedUpCancelType.SpeedUp -> adapter.speedUpTransactionInfo(transactionRecord.transactionHash)
                SpeedUpCancelType.Cancel -> adapter.cancelTransactionInfo(transactionRecord.transactionHash)
            }

            return ResendBitcoinViewModel(
                type = optionType,
                transactionRecord = transactionRecord,
                replacementInfo = replacementInfo,
                adapter = adapter,
                xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency),
                feeRateProvider = FeeRateProviderFactory.provider(adapter.wallet.token.blockchainType)!!,
                contactsRepo = App.contactsRepository
            ) as T
        }
    }

}
