package com.payfunds.wallet.network.response_model.nominee_status

data class Data(
    val nominees: List<Nominee>,
    val walletAddress: String
)