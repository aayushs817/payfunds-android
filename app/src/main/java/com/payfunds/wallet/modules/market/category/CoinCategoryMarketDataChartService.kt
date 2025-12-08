package com.payfunds.wallet.modules.market.category

import io.horizontalsystems.marketkit.models.HsTimePeriod
import com.payfunds.wallet.core.managers.CurrencyManager
import com.payfunds.wallet.core.managers.MarketKitWrapper
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.core.stats.statPeriod
import com.payfunds.wallet.entities.Currency
import com.payfunds.wallet.modules.chart.AbstractChartService
import com.payfunds.wallet.modules.chart.ChartPointsWrapper
import io.payfunds.chartview.ChartViewType
import io.payfunds.chartview.models.ChartPoint
import io.reactivex.Single

class CoinCategoryMarketDataChartService(
    override val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper,
    private val categoryUid: String,
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Day1
    override val chartIntervals = listOf(HsTimePeriod.Day1, HsTimePeriod.Week1, HsTimePeriod.Month1)
    override val chartViewType = ChartViewType.Line

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency
    ): Single<ChartPointsWrapper> = try {
        marketKit.coinCategoryMarketPointsSingle(categoryUid, chartInterval, currency.code)
            .map { info ->
                info.map { ChartPoint(it.marketCap.toFloat(), it.timestamp) }
            }
            .map { ChartPointsWrapper(it) }
    } catch (e: Exception) {
        Single.error(e)
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(
            page = StatPage.CoinCategory,
            event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod)
        )
    }
}
