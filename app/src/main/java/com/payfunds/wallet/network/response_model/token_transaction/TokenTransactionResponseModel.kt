package com.payfunds.wallet.network.response_model.token_transaction

data class TokenTransactionResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)