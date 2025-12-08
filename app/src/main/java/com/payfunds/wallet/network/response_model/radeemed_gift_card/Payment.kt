package com.payfunds.wallet.network.response_model.radeemed_gift_card

data class Payment(
    val amount: String,
    val clientSecret: String,
    val currency: String,
    val id: String,
    val paymentId: String,
    val status: String
)