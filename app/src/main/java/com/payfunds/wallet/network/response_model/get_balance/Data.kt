package com.payfunds.wallet.network.response_model.get_balance

data class Data(
    val accountId: String,
    val balance: Double,
    val currency: String,
    val lastUpdated: String
)