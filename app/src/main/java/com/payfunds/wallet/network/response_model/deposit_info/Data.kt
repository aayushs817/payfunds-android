package com.payfunds.wallet.network.response_model.deposit_info

data class Data(
    val accountId: String,
    val address: String,
    val chainName: String,
    val symbol: String,
    val type: String
)