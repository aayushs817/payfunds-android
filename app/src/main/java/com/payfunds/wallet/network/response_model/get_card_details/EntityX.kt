package com.payfunds.wallet.network.response_model.get_card_details

data class EntityX(
    val `data`: DataX,
    val id: String,
    val isDefault: Boolean,
    val logoUrl: String,
    val symbol: String,
    val token: TokenXX,
    val type: String
)