package com.payfunds.wallet.modules.market.metricspage

import io.horizontalsystems.marketkit.models.HsTimePeriod
import com.payfunds.wallet.core.managers.CurrencyManager
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.core.stats.statPage
import com.payfunds.wallet.core.stats.statPeriod
import com.payfunds.wallet.entities.Currency
import com.payfunds.wallet.modules.chart.AbstractChartService
import com.payfunds.wallet.modules.chart.ChartPointsWrapper
import com.payfunds.wallet.modules.market.tvl.GlobalMarketRepository
import com.payfunds.wallet.modules.metricchart.MetricsType
import io.payfunds.chartview.ChartViewType
import io.reactivex.Single

class MetricsPageChartService(
    override val currencyManager: CurrencyManager,
    private val metricsType: MetricsType,
    private val globalMarketRepository: GlobalMarketRepository,
) : AbstractChartService() {

    override val initialChartInterval: HsTimePeriod = HsTimePeriod.Day1

    override val chartIntervals = listOf(
        HsTimePeriod.Day1,
        HsTimePeriod.Week1,
        HsTimePeriod.Month1,
        HsTimePeriod.Month3,
        HsTimePeriod.Month6,
        HsTimePeriod.Year1,
        HsTimePeriod.Year2,
    )

    override val chartViewType = ChartViewType.Line

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency,
    ): Single<ChartPointsWrapper> {
        return globalMarketRepository.getGlobalMarketPoints(
            currency.code,
            chartInterval,
            metricsType
        ).map {
            ChartPointsWrapper(it)
        }
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(
            page = metricsType.statPage,
            event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod)
        )
    }
}
