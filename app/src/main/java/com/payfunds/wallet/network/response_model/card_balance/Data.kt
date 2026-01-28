package com.payfunds.wallet.network.response_model.card_balance

data class Data(
    val accountId: String,
    val balance: Double,
    val currency: String,
    val lastUpdated: String,
    val message: String
)