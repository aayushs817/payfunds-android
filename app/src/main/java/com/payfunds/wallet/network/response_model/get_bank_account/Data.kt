package com.payfunds.wallet.network.response_model.get_bank_account

data class Data(
    val accountId: String,
    val balance: Int,
    val createdAt: String,
    val currency: String,
    val status: String,
    val type: String
)