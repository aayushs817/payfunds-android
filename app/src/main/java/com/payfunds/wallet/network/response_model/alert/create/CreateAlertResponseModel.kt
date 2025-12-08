package com.payfunds.wallet.network.response_model.alert.create

data class CreateAlertResponseModel(
    val isSuccess: Boolean,
    val `data`: Data,
    val msg: String,
    val result: Int
)