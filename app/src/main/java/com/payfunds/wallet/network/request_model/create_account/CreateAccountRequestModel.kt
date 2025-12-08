package com.payfunds.wallet.network.request_model.create_account

data class CreateAccountRequestModel(
    val walletAddress: String,
    val referralCode: String? = null
)