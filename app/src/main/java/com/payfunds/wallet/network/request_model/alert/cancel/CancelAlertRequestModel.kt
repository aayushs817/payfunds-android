package com.payfunds.wallet.network.request_model.alert.cancel

data class CancelAlertRequestModel(
    val alerts: List<String>,
    val walletAddress: String
)