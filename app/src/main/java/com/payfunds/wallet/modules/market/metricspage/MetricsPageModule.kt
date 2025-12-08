package com.payfunds.wallet.modules.market.metricspage

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.models.FullCoin
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.chart.ChartCurrencyValueFormatterShortened
import com.payfunds.wallet.modules.chart.ChartModule
import com.payfunds.wallet.modules.chart.ChartViewModel
import com.payfunds.wallet.modules.market.MarketDataValue
import com.payfunds.wallet.modules.market.MarketModule
import com.payfunds.wallet.modules.market.tvl.GlobalMarketRepository
import com.payfunds.wallet.modules.metricchart.MetricsType
import java.math.BigDecimal

object MetricsPageModule {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val metricsType: MetricsType) : ViewModelProvider.Factory {
        private val globalMarketRepository by lazy {
            GlobalMarketRepository(App.marketKit)
        }

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                MetricsPageViewModel::class.java -> {
                    MetricsPageViewModel(metricsType, App.currencyManager, App.marketKit) as T
                }

                ChartViewModel::class.java -> {
                    val chartService = MetricsPageChartService(
                        App.currencyManager,
                        metricsType,
                        globalMarketRepository
                    )
                    val chartNumberFormatter = ChartCurrencyValueFormatterShortened()
                    ChartModule.createViewModel(chartService, chartNumberFormatter) as T
                }

                else -> throw IllegalArgumentException()
            }
        }
    }

    @Immutable
    data class CoinViewItem(
        val fullCoin: FullCoin,
        val subtitle: String,
        val coinRate: String,
        val marketDataValue: MarketDataValue?,
        val rank: String?,
        val sortField: BigDecimal?,
    )

    @Immutable
    data class UiState(
        val header: MarketModule.Header,
        val viewItems: List<CoinViewItem>,
        val viewState: ViewState,
        val isRefreshing: Boolean,
        val toggleButtonTitle: String,
        val sortDescending: Boolean,
    )
}

