package com.payfunds.wallet.network.request_model.create_payment

data class Token(
    val amount: String,
    val contract: String? = null,
    val name: String,
    val price: String,
    val symbol: String
)