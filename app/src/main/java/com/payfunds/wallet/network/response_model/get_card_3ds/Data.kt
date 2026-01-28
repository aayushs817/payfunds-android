package com.payfunds.wallet.network.response_model.get_card_3ds

data class Data(
    val forwardingMethod: String,
    val cardId: String,
)