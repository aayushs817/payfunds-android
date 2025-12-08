package com.payfunds.wallet.network.response_model.balance

data class BalanceResponseModel(
    val message: String,
    val result: String?,
    val status: String
)