package com.payfunds.wallet.network.response_model.transaction_history

data class TxnHistory(
    val amount: Double,
    val from: String,
    val timestamp: String,
    val to: String,
    val token: Token,
    val transactionHash: String? = null,
    val transactionType: String
)