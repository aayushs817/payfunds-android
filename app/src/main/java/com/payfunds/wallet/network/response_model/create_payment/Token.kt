package com.payfunds.wallet.network.response_model.create_payment

data class Token(
    val amount: String,
    val contract: String,
    val name: String,
    val price: String,
    val symbol: String
)