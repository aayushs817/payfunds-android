package com.payfunds.wallet.network.request_model.alert.create

data class CreateAlertRequestModel(
    val frequency: String,
    val price: String,
    val token: Token,
    val type: String,
    val walletAddress: String
)