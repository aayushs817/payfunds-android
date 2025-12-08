package com.payfunds.wallet.modules.market.platform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.payfunds.wallet.core.App
import com.payfunds.wallet.modules.chart.ChartCurrencyValueFormatterShortened
import com.payfunds.wallet.modules.chart.ChartModule
import com.payfunds.wallet.modules.chart.ChartViewModel
import com.payfunds.wallet.modules.market.MarketField
import com.payfunds.wallet.modules.market.SortingField
import com.payfunds.wallet.modules.market.topplatforms.Platform
import com.payfunds.wallet.ui.compose.Select

object MarketPlatformModule {

    class Factory(private val platform: Platform) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                MarketPlatformViewModel::class.java -> {
                    val repository =
                        MarketPlatformCoinsRepository(platform, App.marketKit, App.currencyManager)
                    MarketPlatformViewModel(platform, repository, App.marketFavoritesManager) as T
                }

                ChartViewModel::class.java -> {
                    val chartService =
                        PlatformChartService(platform, App.currencyManager, App.marketKit)
                    val chartNumberFormatter = ChartCurrencyValueFormatterShortened()
                    ChartModule.createViewModel(chartService, chartNumberFormatter) as T
                }

                else -> throw IllegalArgumentException()
            }
        }

    }

    data class Menu(
        val sortingFieldSelect: Select<SortingField>,
        val marketFieldSelect: Select<MarketField>
    )

}
