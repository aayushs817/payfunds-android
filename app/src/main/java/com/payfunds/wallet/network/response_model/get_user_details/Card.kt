package com.payfunds.wallet.network.response_model.get_user_details

data class Card(
    val cardId: String,
    val freeze: Boolean,
    val hasPin: Boolean,
    val limit: Int,
    val status: String,
    val type: String
)