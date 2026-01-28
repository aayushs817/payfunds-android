package com.payfunds.wallet.network.response_model.get_card_details

data class Escrow(
    val bankAccountTypeDisplay: String,
    val status: Boolean,
    val value: Int
)