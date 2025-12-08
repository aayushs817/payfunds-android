package com.payfunds.wallet.network.response_model.alert.list

data class Data(
    val totalCount: Int,
    val alerts: List<Alert>
)