package com.payfunds.wallet.network.request_model.add_token

data class AddTokenDetail(
    val ticker: String,
    val contract: String?,
    val walletAddress: String,
    val network: String
)