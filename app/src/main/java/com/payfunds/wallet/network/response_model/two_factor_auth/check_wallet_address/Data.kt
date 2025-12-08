package com.payfunds.wallet.network.response_model.two_factor_auth.check_wallet_address

data class Data(
    val _id: String,
    val multiFactor: MultiFactor,
    val walletAddress: String
)