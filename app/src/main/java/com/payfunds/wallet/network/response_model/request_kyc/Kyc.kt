package com.payfunds.wallet.network.response_model.request_kyc

data class Kyc(
    val reference: String,
    val status: String,
    val verification_url: String
)