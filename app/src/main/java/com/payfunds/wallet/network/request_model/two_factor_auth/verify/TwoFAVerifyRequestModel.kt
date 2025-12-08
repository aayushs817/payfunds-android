package com.payfunds.wallet.network.request_model.two_factor_auth.verify

data class TwoFAVerifyRequestModel(
    val otp: String,
    val walletAddress: String
)