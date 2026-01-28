package com.payfunds.wallet.network.response_model.get_card_details

data class Currency(
    val createdAt: String,
    val id: String,
    val isDefault: Boolean,
    val isDefaultReward: Boolean,
    val logoUrl: String,
    val token: Token,
    val type: String,
    val underlyingCurrencyId: Any,
    val updatedAt: String
)