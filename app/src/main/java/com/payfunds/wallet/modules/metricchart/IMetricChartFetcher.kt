package com.payfunds.wallet.modules.metricchart

import com.payfunds.wallet.ui.compose.TranslatableString

interface IMetricChartFetcher {
    val title: Int
    val description: TranslatableString
    val poweredBy: TranslatableString
}