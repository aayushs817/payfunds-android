package com.payfunds.wallet.network.response_model.token_transaction

data class Token(
    val amount: String,
    val contract: String,
    val name: String,
    val price: String,
    val symbol: String,
    val transactionFee: String,
    val transactionHash: String,
    val transferStatus: String
)