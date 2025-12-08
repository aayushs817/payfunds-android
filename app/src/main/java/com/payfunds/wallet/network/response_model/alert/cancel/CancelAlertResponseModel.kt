package com.payfunds.wallet.network.response_model.alert.cancel

data class CancelAlertResponseModel(
    val isSuccess: Boolean,
    val `data`: Data,
    val msg: String,
    val result: Int
)