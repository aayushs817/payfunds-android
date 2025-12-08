package com.payfunds.wallet.ui.extensions

import com.payfunds.wallet.modules.metricchart.MetricsType
import io.payfunds.chartview.ChartData
import java.math.BigDecimal

data class MetricData(
    val value: String?,
    val diff: BigDecimal?,
    val chartData: ChartData?,
    val type: MetricsType
)
