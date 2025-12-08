package com.payfunds.wallet.network.response_model.kyc_status

data class Data(
    val proof: Proof,
    val reference: String,
    val status: String,
    val verification_url: String
)