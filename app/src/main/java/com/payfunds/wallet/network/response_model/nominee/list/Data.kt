package com.payfunds.wallet.network.response_model.nominee.list

data class Data(
    val _id: String,
    val nominees: List<Nominee>,
    val walletAddress: String,
    val email: String,
)