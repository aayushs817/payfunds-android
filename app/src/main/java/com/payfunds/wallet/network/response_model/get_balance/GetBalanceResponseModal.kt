package com.payfunds.wallet.network.response_model.get_balance

data class GetBalanceResponseModal(
    val `data`: Data,
    val message: String,
    val success: Boolean
)