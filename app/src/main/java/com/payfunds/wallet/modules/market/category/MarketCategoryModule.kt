package com.payfunds.wallet.modules.market.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.models.Analytics
import io.horizontalsystems.marketkit.models.CoinCategory
import com.payfunds.wallet.core.App
import com.payfunds.wallet.modules.chart.ChartCurrencyValueFormatterShortened
import com.payfunds.wallet.modules.chart.ChartModule
import com.payfunds.wallet.modules.chart.ChartViewModel
import com.payfunds.wallet.modules.market.MarketField
import com.payfunds.wallet.modules.market.MarketItem
import com.payfunds.wallet.modules.market.SortingField
import com.payfunds.wallet.modules.market.TopMarket
import com.payfunds.wallet.ui.compose.Select

object MarketCategoryModule {

    class Factory(
        private val coinCategory: CoinCategory
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                MarketCategoryViewModel::class.java -> {
                    val marketCategoryRepository = MarketCategoryRepository(App.marketKit)
                    val service = MarketCategoryService(
                        marketCategoryRepository,
                        App.currencyManager,
                        App.languageManager,
                        App.marketFavoritesManager,
                        coinCategory,
                        defaultTopMarket,
                        defaultSortingField
                    )
                    MarketCategoryViewModel(service) as T
                }

                ChartViewModel::class.java -> {
                    val chartService = CoinCategoryMarketDataChartService(
                        App.currencyManager,
                        App.marketKit,
                        coinCategory.uid
                    )
                    val chartNumberFormatter = ChartCurrencyValueFormatterShortened()
                    ChartModule.createViewModel(chartService, chartNumberFormatter) as T
                }

                else -> throw IllegalArgumentException()
            }
        }

        companion object {
            val defaultSortingField = SortingField.HighestCap
            val defaultTopMarket = TopMarket.Top100
        }
    }

    data class Menu(
        val sortingFieldSelect: Select<SortingField>,
        val marketFieldSelect: Select<MarketField>
    )

}

data class MarketItemWrapper(
    val marketItem: MarketItem,
    val favorited: Boolean,
    val signal: Analytics.TechnicalAdvice.Advice? = null
)