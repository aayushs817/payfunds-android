package com.payfunds.wallet.network.response_model.get_user_details

data class Account(
    val accountId: String,
    val balance: Double,
    val currency: String,
    val type: String
)