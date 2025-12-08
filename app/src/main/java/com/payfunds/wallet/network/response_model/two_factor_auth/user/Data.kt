package com.payfunds.wallet.network.response_model.two_factor_auth.user

data class Data(
    val _id: String,
    val accountTypeCode: String,
    val createdAt: String,
    val isMultiFactor: Boolean,
    val multiFactor: MultiFactor,
    val recordExist: Boolean,
    val token: String,
    val updatedAt: String,
    val walletAddress: String
)