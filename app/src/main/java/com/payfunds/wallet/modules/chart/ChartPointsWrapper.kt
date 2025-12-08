package com.payfunds.wallet.modules.chart

import io.payfunds.chartview.models.ChartIndicator
import io.payfunds.chartview.models.ChartPoint

data class ChartPointsWrapper(
    val items: List<ChartPoint>,
    val isMovementChart: Boolean = true,
    val indicators: Map<String, ChartIndicator> = mapOf(),
)
