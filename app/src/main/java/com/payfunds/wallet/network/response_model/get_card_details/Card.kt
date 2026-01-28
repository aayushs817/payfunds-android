package com.payfunds.wallet.network.response_model.get_card_details

data class Card(
    val card: CardX,
    val message: String,
    val success: Boolean
)