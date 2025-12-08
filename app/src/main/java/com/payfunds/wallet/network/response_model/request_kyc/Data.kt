package com.payfunds.wallet.network.response_model.request_kyc

data class Data(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val kyc: Kyc,
    val stripe: Stripe,
    val updatedAt: String,
    val walletAddress: String
)