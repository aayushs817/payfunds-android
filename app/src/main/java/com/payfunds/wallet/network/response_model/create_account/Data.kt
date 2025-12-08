package com.payfunds.wallet.network.response_model.create_account

data class Data(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val kyc: Kyc,
    val stripe: Stripe,
    val referralCode: String,
    val updatedAt: String,
    val walletAddress: String
)