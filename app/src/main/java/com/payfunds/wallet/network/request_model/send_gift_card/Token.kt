package com.payfunds.wallet.network.request_model.send_gift_card

data class Token(
    val amount: String,
    val name: String,
    val symbol: String
)