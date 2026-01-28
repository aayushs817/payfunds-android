package com.payfunds.wallet.network.response_model.get_card_info

data class Card(
    val cvvText: String,
    val expiry: String,
    val panText: String
)