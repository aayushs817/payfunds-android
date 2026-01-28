package com.payfunds.wallet.network.response_model.request_card

data class Data(
    val createdAt: String,
    val requestId: String,
    val status: String,
    val type: String
)