package com.payfunds.wallet.network.response_model.nominee.document

data class Data(
    val nominee: Nominee,
    val password: String? = null,
    val walletAddress: String
)