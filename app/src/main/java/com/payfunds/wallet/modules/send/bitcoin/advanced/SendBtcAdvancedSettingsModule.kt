package com.payfunds.wallet.modules.send.bitcoin.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.TransactionDataSortMode

object SendBtcAdvancedSettingsModule {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val blockchainType: BlockchainType) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SendBtcAdvancedSettingsViewModel(
                blockchainType,
                App.btcBlockchainManager,
                App.localStorage
            ) as T
        }
    }

    data class UiState(
        val transactionSortOptions: List<SortModeViewItem>,
        val transactionSortTitle: String,
        val utxoExpertModeEnabled: Boolean,
        val rbfEnabled: Boolean,
    )

    data class SortModeViewItem(
        val mode: TransactionDataSortMode,
        val selected: Boolean,
    )
}
