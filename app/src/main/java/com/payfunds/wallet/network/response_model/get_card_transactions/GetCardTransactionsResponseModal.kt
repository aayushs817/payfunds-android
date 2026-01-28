package com.payfunds.wallet.network.response_model.get_card_transactions

data class GetCardTransactionsResponseModal(
    val `data`: Data,
    val message: String,
    val success: Boolean
)