package com.payfunds.wallet.modules.chart

import io.horizontalsystems.marketkit.models.HsTimePeriod
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.coin.ChartInfoData
import com.payfunds.wallet.ui.compose.components.TabItem
import io.payfunds.chartview.ChartViewType

data class ChartUiState(
    val tabItems: List<TabItem<HsTimePeriod?>>,
    val chartHeaderView: ChartModule.ChartHeaderView?,
    val chartInfoData: ChartInfoData?,
    val loading: Boolean,
    val viewState: ViewState,
    val hasVolumes: Boolean,
    val chartViewType: ChartViewType,
)
