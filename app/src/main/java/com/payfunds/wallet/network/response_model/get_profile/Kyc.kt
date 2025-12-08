package com.payfunds.wallet.network.response_model.get_profile

data class Kyc(
    val proof: Proof,
    val status: String,
    val document: Document,
    val reference: String,
    val retries: Int,
    val verification_url: String
)