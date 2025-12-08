package com.payfunds.wallet.network.request_model.send_gift_card

data class Payment(
    val amount: String,
    val currency: String
)