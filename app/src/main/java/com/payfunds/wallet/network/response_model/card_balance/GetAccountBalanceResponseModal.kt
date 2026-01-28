package com.payfunds.wallet.network.response_model.card_balance

data class GetAccountBalanceResponseModal(
    val `data`: Data,
    val message: String,
    val success: Boolean
)