package com.payfunds.wallet.modules.market

import android.util.Log
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.marketkit.models.MarketGlobal
import com.payfunds.wallet.core.ILocalStorage
import com.payfunds.wallet.core.IMarketStorage
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.managers.CurrencyManager
import com.payfunds.wallet.core.managers.MarketKitWrapper
import com.payfunds.wallet.entities.LaunchPage
import com.payfunds.wallet.modules.market.MarketModule.Tab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await

class MarketViewModel(
    private val marketStorage: IMarketStorage,
    private val marketKit: MarketKitWrapper,
    private val currencyManager: CurrencyManager,
    localStorage: ILocalStorage
) : ViewModelUiState<MarketModule.UiState>() {

    val tabs = Tab.entries.toTypedArray()
    private var currency = currencyManager.baseCurrency

    private var marketGlobal: MarketGlobal? = null
    private var selectedTab: Tab = getInitialTab(localStorage.launchPage)

    init {
        viewModelScope.launch {
            currencyManager.baseCurrencyUpdatedFlow.collect {
                currency = currencyManager.baseCurrency
                emitState()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                marketGlobal = marketKit.marketGlobalSingle(currency.code).await()
                emitState()
            } catch (e: Throwable) {
                Log.e("TAG", "updateMarketOverview: ", e)
            }
        }
    }

    override fun createState() = MarketModule.UiState(
        selectedTab = selectedTab,
        marketGlobal = marketGlobal,
        currency = currency
    )

    fun onSelect(tab: Tab) {
        selectedTab = tab
        marketStorage.currentMarketTab = tab
        emitState()
    }

    private fun getInitialTab(launchPage: LaunchPage?) = when (launchPage) {
        LaunchPage.Watchlist -> Tab.Watchlist
        else -> marketStorage.currentMarketTab ?: Tab.Coins
    }
}