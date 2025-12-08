package com.payfunds.wallet.modules.nft.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.CoinValue
import com.payfunds.wallet.entities.CurrencyValue
import com.payfunds.wallet.entities.nft.NftUid
import com.payfunds.wallet.modules.balance.BalanceXRateRepository
import com.payfunds.wallet.modules.balance.TotalBalance
import com.payfunds.wallet.modules.balance.TotalService

object NftHoldingsModule {
    class Factory(
        private val account: Account
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val totalService = TotalService(
                App.currencyManager,
                App.marketKit,
                App.baseTokenManager,
                App.balanceHiddenManager
            )
            val xRateRepository =
                BalanceXRateRepository("nft-holding", App.currencyManager, App.marketKit)
            val service = NftHoldingsService(
                account,
                App.nftAdapterManager,
                App.nftMetadataManager,
                App.nftMetadataSyncer,
                xRateRepository
            )
            return NftHoldingsViewModel(
                service,
                TotalBalance(totalService, App.balanceHiddenManager)
            ) as T
        }
    }
}

data class NftCollectionViewItem(
    val uid: String,
    val name: String,
    val imageUrl: String?,
    val count: Int,
    val expanded: Boolean,
    val assets: List<NftAssetViewItem>
)

data class NftAssetViewItem(
    val collectionUid: String?,
    val nftUid: NftUid,
    val name: String,
    val imageUrl: String?,
    val count: Int,
    val onSale: Boolean,
    val price: CoinValue?,
    val priceInFiat: CurrencyValue?
)