package com.payfunds.wallet.network.response_model.get_deposit

data class Data(
    val accountId: String,
    val currency: String,
    val depositAddress: String,
    val lastUpdated: String,
    val minDeposit: Double,
    val network: String
)