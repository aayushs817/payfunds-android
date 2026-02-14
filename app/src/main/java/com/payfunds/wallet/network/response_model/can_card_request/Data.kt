package com.payfunds.wallet.network.response_model.can_card_request

data class Data(
    val canRequest: Boolean,
    val cardPrice: String,
    val depositInfo: DepositInfo,
    val message: String
)