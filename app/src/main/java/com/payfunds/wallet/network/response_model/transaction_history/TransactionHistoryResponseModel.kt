package com.payfunds.wallet.network.response_model.transaction_history

data class TransactionHistoryResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)