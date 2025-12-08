package com.payfunds.wallet.modules.balance.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.balance.BalanceAdapterRepository
import com.payfunds.wallet.modules.balance.BalanceCache
import com.payfunds.wallet.modules.balance.BalanceViewItem
import com.payfunds.wallet.modules.balance.BalanceViewItemFactory
import com.payfunds.wallet.modules.balance.BalanceXRateRepository
import com.payfunds.wallet.modules.transactions.NftMetadataService
import com.payfunds.wallet.modules.transactions.TransactionRecordRepository
import com.payfunds.wallet.modules.transactions.TransactionSyncStateRepository
import com.payfunds.wallet.modules.transactions.TransactionViewItem
import com.payfunds.wallet.modules.transactions.TransactionViewItemFactory
import com.payfunds.wallet.modules.transactions.TransactionsRateRepository

class TokenBalanceModule {

    class Factory(private val wallet: Wallet) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val balanceService = TokenBalanceService(
                wallet,
                BalanceXRateRepository("wallet", App.currencyManager, App.marketKit),
                BalanceAdapterRepository(
                    App.adapterManager,
                    BalanceCache(App.appDatabase.enabledWalletsCacheDao())
                ),
            )

            val tokenTransactionsService = TokenTransactionsService(
                wallet,
                TransactionRecordRepository(App.transactionAdapterManager),
                TransactionsRateRepository(App.currencyManager, App.marketKit),
                TransactionSyncStateRepository(App.transactionAdapterManager),
                App.contactsRepository,
                NftMetadataService(App.nftMetadataManager),
                App.spamManager
            )

            return TokenBalanceViewModel(
                wallet,
                balanceService,
                BalanceViewItemFactory(),
                tokenTransactionsService,
                TransactionViewItemFactory(
                    App.evmLabelManager,
                    App.contactsRepository,
                    App.balanceHiddenManager
                ),
                App.balanceHiddenManager,
                App.connectivityManager,
                App.accountManager,
            ) as T
        }
    }

    data class TokenBalanceUiState(
        val title: String,
        val balanceViewItem: BalanceViewItem?,
        val transactions: Map<String, List<TransactionViewItem>>?,
    )
}
