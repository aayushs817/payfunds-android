package com.payfunds.wallet.network.request_model.send_gift_card

data class Receiver(
    val email: String? = null,
    val walletAddress: String? = null
)