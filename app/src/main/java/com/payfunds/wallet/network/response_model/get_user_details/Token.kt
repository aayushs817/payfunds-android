package com.payfunds.wallet.network.response_model.get_user_details

data class Token(
    val chainId: Int,
    val contractAddress: String,
    val decimals: Int,
    val isNative: Boolean,
    val lastKnownBalance: String,
    val symbol: String
)