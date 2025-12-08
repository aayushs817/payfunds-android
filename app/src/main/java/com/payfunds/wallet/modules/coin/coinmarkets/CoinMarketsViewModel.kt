package com.payfunds.wallet.modules.coin.coinmarkets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.DataState
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.coin.MarketTickerViewItem
import com.payfunds.wallet.ui.compose.TranslatableString
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class CoinMarketsViewModel(private val service: CoinMarketsService) : ViewModel() {
    val verifiedMenu by service::verifiedMenu
    val viewStateLiveData = MutableLiveData<ViewState>(ViewState.Loading)
    val viewItemsLiveData = MutableLiveData<List<MarketTickerViewItem>>()

    init {
        viewModelScope.launch {
            service.stateObservable.asFlow().collect {
                syncState(it)
            }
        }

        service.start()
    }

    private fun syncState(state: DataState<List<MarketTickerItem>>) {
        viewStateLiveData.postValue(state.viewState)

        state.dataOrNull?.let { data ->
            viewItemsLiveData.postValue(data.map { createViewItem(it) })
        }
    }

    private fun createViewItem(item: MarketTickerItem): MarketTickerViewItem {
        return MarketTickerViewItem(
            item.market,
            item.marketImageUrl,
            "${item.baseCoinCode}/${item.targetCoinCode}",
            App.numberFormatter.formatFiatShort(
                item.volumeFiat,
                service.currency.symbol,
                service.currency.decimal
            ),
            App.numberFormatter.formatCoinShort(item.volumeToken, item.baseCoinCode, 8),
            item.tradeUrl,
            if (item.verified) TranslatableString.ResString(R.string.CoinPage_MarketsLabel_Verified) else null
        )
    }

    override fun onCleared() {
        service.stop()
    }

    fun onErrorClick() {
        service.start()
    }

    fun toggleVerifiedType(verifiedType: VerifiedType) {
        service.setVerifiedType(verifiedType)
    }

}
