package com.payfunds.wallet.modules.coin.majorholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.models.Blockchain
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.coin.CoinViewFactory
import com.payfunds.wallet.modules.coin.MajorHolderItem
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.StackBarSlice

object CoinMajorHoldersModule {
    class Factory(private val coinUid: String, private val blockchain: Blockchain) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val factory = CoinViewFactory(App.currencyManager.baseCurrency, App.numberFormatter)
            return CoinMajorHoldersViewModel(coinUid, blockchain, App.marketKit, factory) as T
        }
    }

    data class UiState(
        val viewState: ViewState,
        val top10Share: String,
        val totalHoldersCount: String,
        val seeAllUrl: String?,
        val chartData: List<StackBarSlice>,
        val topHolders: List<MajorHolderItem>,
        val error: TranslatableString?,
    )
}
