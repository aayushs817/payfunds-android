package com.payfunds.wallet.network.response_model.get_card_transactions

data class Transaction(
    val amount: Double,
    val createdAt: String,
    val currency: String,
    val status: String,
    val transactionId: String,
    val type: String
)