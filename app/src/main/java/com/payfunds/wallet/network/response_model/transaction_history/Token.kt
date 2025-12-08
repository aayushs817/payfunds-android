package com.payfunds.wallet.network.response_model.transaction_history

data class Token(
    val address: String,
    val decimals: String,
    val icon_url: String,
    val name: String,
    val symbol: String,
    val type: String
)