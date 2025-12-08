package com.payfunds.wallet.network.response_model.token_transaction

data class Data(
    val totalCount: Int,
    val transactions: List<Transaction>
)