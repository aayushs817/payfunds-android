package com.payfunds.wallet.network.response_model.two_factor_auth.verify

data class Data(
    val _id: String,
    val accountTypeCode: String,
    val createdAt: String,
    val multiFactor: MultiFactor,
    val token: String,
    val updatedAt: String,
    val walletAddress: String
)