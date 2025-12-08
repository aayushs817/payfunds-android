package com.payfunds.wallet.network.response_model.radeemed_gift_card

data class Token(
    val amount: String,
    val name: String,
    val symbol: String,
    val transactionHash: String,
    val transferStatus: String
)